package com.example.grzy.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbOpenSource;
import com.example.grzy.mapper.TbOpenSourceMapper;
import com.example.grzy.service.OpenSourceService;
import com.example.grzy.service.OperLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 开源项目服务实现
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSourceServiceImpl extends ServiceImpl<TbOpenSourceMapper, TbOpenSource> implements OpenSourceService {

    private final OperLogService operLogService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 仓库平台白名单：仅允许这两家的公开 API（天然防 SSRF） */
    private static final String GITEE_HOST = "gitee.com";
    private static final String GITHUB_HOST = "github.com";
    private static final String GITEE_API = "https://gitee.com/api/v5/repos/%s/%s";
    private static final String GITHUB_API = "https://api.github.com/repos/%s/%s";

    /** 同步频控 Redis Key 前缀 */
    private static final String SYNC_LIMIT_KEY = "grzy:opensource:sync:";
    /** 同步最小间隔（分钟） */
    private static final int SYNC_INTERVAL_MINUTES = 10;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public List<TbOpenSource> getFrontList() {
        return this.list(new LambdaQueryWrapper<TbOpenSource>()
                .orderByDesc(TbOpenSource::getIsTop)
                .orderByAsc(TbOpenSource::getSort)
                .orderByDesc(TbOpenSource::getCreateTime));
    }

    @Override
    public PageResult<TbOpenSource> getAdminPage(Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<TbOpenSource> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TbOpenSource::getStatus, status);
        }
        wrapper.orderByDesc(TbOpenSource::getIsTop)
                .orderByAsc(TbOpenSource::getSort)
                .orderByDesc(TbOpenSource::getCreateTime);
        IPage<TbOpenSource> result = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TbOpenSource item) {
        validate(item);
        item.setId(null);
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        item.setIsTop(item.getIsTop() != null ? item.getIsTop() : 0);
        item.setSort(item.getSort() != null ? item.getSort() : 0);
        item.setStars(item.getStars() != null ? item.getStars() : 0);
        item.setForks(item.getForks() != null ? item.getForks() : 0);
        this.save(item);
        operLogService.log("开源项目", "新增了开源项目: " + item.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TbOpenSource item) {
        TbOpenSource existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        validate(item);
        item.setId(id);
        this.updateById(item);
        operLogService.log("开源项目", "更新了开源项目: " + item.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TbOpenSource existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        this.removeById(id);
        operLogService.log("开源项目", "删除了开源项目: " + existing.getName());
    }

    @Override
    public TbOpenSource syncFromRepo(Long id) {
        TbOpenSource item = this.getById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 频控：10 分钟内同一项目只允许同步一次（防连点刷第三方 API 限额）
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(
                SYNC_LIMIT_KEY + id, "1", Duration.ofMinutes(SYNC_INTERVAL_MINUTES));
        if (!Boolean.TRUE.equals(ok)) {
            throw new BusinessException("同步太频繁，请 " + SYNC_INTERVAL_MINUTES + " 分钟后再试");
        }

        // 解析仓库地址
        String[] ownerRepo = parseOwnerRepo(item.getRepoUrl());
        String platform = ownerRepo[2];
        String api = GITEE_HOST.equals(platform)
                ? String.format(GITEE_API, ownerRepo[0], ownerRepo[1])
                : String.format(GITHUB_API, ownerRepo[0], ownerRepo[1]);

        String body = fetch(api, platform);
        JsonNode json;
        try {
            json = OBJECT_MAPPER.readTree(body);
        } catch (Exception e) {
            throw new BusinessException("仓库平台返回数据解析失败，请稍后重试");
        }

        // 同步字段：star/fork 必同步；license API 有则覆盖；description 仅当本地为空时补
        TbOpenSource update = new TbOpenSource();
        update.setId(id);
        update.setStars(json.path("stargazers_count").asInt(item.getStars() != null ? item.getStars() : 0));
        update.setForks(json.path("forks_count").asInt(item.getForks() != null ? item.getForks() : 0));
        String license = json.path("license").isTextual()
                ? json.path("license").asText(null)
                : json.path("license").path("spdx_id").asText(null);
        if (StrUtil.isNotBlank(license) && !"null".equals(license)) {
            update.setLicense(license);
        }
        if (StrUtil.isBlank(item.getDescription()) && json.path("description").isTextual()) {
            update.setDescription(json.path("description").asText());
        }
        update.setSyncTime(LocalDateTime.now());
        this.updateById(update);
        operLogService.log("开源项目", "同步了开源项目数据: " + item.getName());

        return this.getById(id);
    }

    /**
     * 从仓库 URL 解析 owner/repo/平台
     *
     * @return [owner, repo, platform]
     */
    private String[] parseOwnerRepo(String repoUrl) {
        if (StrUtil.isBlank(repoUrl)) {
            throw new BusinessException("仓库地址为空，无法同步");
        }
        try {
            URI uri = URI.create(repoUrl.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            if (!GITEE_HOST.equals(host) && !GITHUB_HOST.equals(host)) {
                throw new BusinessException("仅支持 Gitee / GitHub 仓库地址同步");
            }
            String path = uri.getPath() == null ? "" : uri.getPath();
            // 去掉 .git 后缀与首尾斜杠
            path = path.replaceAll("\\.git$", "").replaceAll("^/+|/+$", "");
            String[] segments = path.split("/");
            if (segments.length < 2 || segments[0].isBlank() || segments[1].isBlank()) {
                throw new BusinessException("仓库地址格式不正确，应为 https://github.com/用户名/仓库名");
            }
            return new String[]{segments[0], segments[1], host};
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("仓库地址解析失败，请检查格式");
        }
    }

    /**
     * 调用仓库平台公开 API
     */
    private String fetch(String api, String platform) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(api))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET();
            // GitHub 匿名 API 必须带 User-Agent，否则 403
            if (GITHUB_HOST.equals(platform)) {
                builder.header("User-Agent", "grzy-personal-site");
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new BusinessException("仓库不存在或为私有仓库，请检查地址");
            }
            if (response.statusCode() == 403) {
                throw new BusinessException("仓库平台接口限流，请稍后再试");
            }
            if (response.statusCode() != 200) {
                throw new BusinessException("仓库平台返回异常状态: " + response.statusCode());
            }
            return response.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("同步开源项目数据失败: api={}, {}", api, e.getMessage());
            throw new BusinessException("无法连接仓库平台，请稍后重试");
        }
    }

    /**
     * 基础字段校验
     */
    private void validate(TbOpenSource item) {
        if (StrUtil.isBlank(item.getName())) {
            throw new BusinessException("项目名称不能为空");
        }
        if (StrUtil.isBlank(item.getDescription())) {
            throw new BusinessException("项目简介不能为空");
        }
        if (StrUtil.isBlank(item.getRepoUrl())) {
            throw new BusinessException("仓库地址不能为空");
        }
        // 提前校验仓库地址可解析（不发起网络请求）
        parseOwnerRepo(item.getRepoUrl());
    }
}
