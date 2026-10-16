package com.example.grzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.grzy.entity.TbVisitorLocation;
import com.example.grzy.mapper.TbVisitorLocationMapper;
import com.example.grzy.service.VisitorLocationService;
import com.example.grzy.vo.VisitorLocationVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访客位置服务实现 — 使用 ip2region 离线数据库
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorLocationServiceImpl implements VisitorLocationService {

    private final TbVisitorLocationMapper visitorLocationMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RECORDED_KEY = "grzy:visitor:recorded:";

    /** ip2region 查询器 */
    private Searcher searcher;

    @Value("${ip2region.db-path:ip2region/ip2region.xdb}")
    private String dbPath;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(dbPath);
            try (InputStream is = resource.getInputStream()) {
                byte[] cBuff = is.readAllBytes();
                searcher = Searcher.newWithBuffer(cBuff);
            }
            log.info("ip2region 数据库加载成功: {}", dbPath);
        } catch (Exception e) {
            log.error("ip2region 数据库加载失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (searcher != null) {
                searcher.close();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    @Async
    public void recordVisitorLocation(String visitorId, String clientIp) {
        try {
            // 本地 IP 不记录
            if (isLocalIp(clientIp)) {
                return;
            }

            // 今天同一访客只记录一次
            String today = java.time.LocalDate.now().toString();
            String recordedKey = RECORDED_KEY + visitorId + ":" + today;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(recordedKey))) {
                return;
            }

            // 使用 ip2region 查询地理位置
            if (searcher == null) {
                log.warn("ip2region 未初始化，跳过访客位置记录");
                return;
            }

            // ip2region 返回格式: 国家|区域|省份|城市|ISP
            String region;
            synchronized (searcher) {
                region = searcher.search(clientIp);
            }
            if (region == null || region.isEmpty()) {
                return;
            }

            String[] parts = region.split("\\|");
            // parts: [0]=国家 [1]=区域 [2]=省份 [3]=城市 [4]=ISP
            String country = parts.length > 0 ? parts[0] : "";
            String province = parts.length > 2 ? parts[2] : "";
            String city = parts.length > 3 ? parts[3] : "";

            // 过滤掉 "0" 值（ip2region 用 "0" 表示未知）
            if ("0".equals(country)) country = "";
            if ("0".equals(province)) province = "";
            if ("0".equals(city)) city = "";

            // 保存到数据库
            TbVisitorLocation entity = new TbVisitorLocation();
            entity.setVisitorId(visitorId);
            entity.setIp(clientIp);
            entity.setCountry(country);
            entity.setProvince(province);
            entity.setCity(city);
            entity.setLongitude(null);
            entity.setLatitude(null);
            entity.setVisitTime(LocalDateTime.now());

            visitorLocationMapper.insert(entity);

            // 标记今天已记录
            redisTemplate.opsForValue().set(recordedKey, "1", 24, TimeUnit.HOURS);

            log.info("访客位置记录成功: ip={}, country={}, province={}, city={}",
                    clientIp, country, province, city);
        } catch (Exception e) {
            log.warn("记录访客位置失败: ip={}, error={}", clientIp, e.getMessage());
        }
    }

    /** 前台地图打点最大返回条数（防全表拖取） */
    private static final int MAX_FRONT_LOCATIONS = 500;

    @Override
    public List<VisitorLocationVO> getAllLocations() {
        return visitorLocationMapper.selectList(
                new LambdaQueryWrapper<TbVisitorLocation>()
                        .orderByDesc(TbVisitorLocation::getVisitTime)
                        .last("LIMIT " + MAX_FRONT_LOCATIONS)
        ).stream().map(this::toVo).collect(Collectors.toList());
    }

    @Override
    public List<VisitorLocationVO> getRecentLocations(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_FRONT_LOCATIONS);
        return visitorLocationMapper.selectList(
                new LambdaQueryWrapper<TbVisitorLocation>()
                        .orderByDesc(TbVisitorLocation::getVisitTime)
                        .last("LIMIT " + safeLimit)
        ).stream().map(this::toVo).collect(Collectors.toList());
    }

    /**
     * 实体转脱敏 VO（剥离 ip 与 visitorId）
     */
    private VisitorLocationVO toVo(TbVisitorLocation entity) {
        VisitorLocationVO vo = new VisitorLocationVO();
        vo.setId(entity.getId());
        vo.setCountry(entity.getCountry());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setLongitude(entity.getLongitude());
        vo.setLatitude(entity.getLatitude());
        vo.setVisitTime(entity.getVisitTime());
        return vo;
    }

    @Override
    public List<Map<String, Object>> getCityStats() {
        // SQL GROUP BY 聚合，避免访客表全量载入内存（表只增不减）
        return toStatsList(visitorLocationMapper.selectCityStats(), "city");
    }

    @Override
    public List<Map<String, Object>> getCountryStats() {
        return toStatsList(visitorLocationMapper.selectCountryStats(), "country");
    }

    /**
     * 聚合结果行（维度列 + cnt）转输出结构，SQL 已按数量倒序
     */
    private List<Map<String, Object>> toStatsList(List<Map<String, Object>> rows, String dimKey) {
        return rows.stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put(dimKey, row.get(dimKey));
                    map.put("count", row.get("cnt"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为本地 IP
     */
    private boolean isLocalIp(String ip) {
        return ip == null
                || ip.isEmpty()
                || "127.0.0.1".equals(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)
                || "localhost".equals(ip);
    }
}
