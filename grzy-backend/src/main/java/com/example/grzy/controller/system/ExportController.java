package com.example.grzy.controller.system;

import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.entity.SysUser;
import com.example.grzy.mapper.TbArticleMapper;
import com.example.grzy.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@Tag(name = "数据导出", description = "后台数据导出接口")
@RestController
@RequestMapping("/api/system/export")
@RequiredArgsConstructor
public class ExportController {

    private final TbArticleMapper articleMapper;
    private final SysUserMapper userMapper;

    @Operation(summary = "导出文章列表", description = "返回文章列表数据用于CSV导出")
    @GetMapping("/articles")
    public Result<List<Map<String, Object>>> exportArticles() {
        List<TbArticle> articles = articleMapper.selectList(
                new LambdaQueryWrapper<TbArticle>()
                        .eq(TbArticle::getDeleted, 0)
                        .orderByDesc(TbArticle::getCreateTime)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (TbArticle a : articles) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ID", a.getId());
            row.put("标题", a.getTitle());
            row.put("分类", a.getCategory());
            row.put("状态", a.getStatus() == 1 ? "已发布" : "已下架");
            row.put("阅读量", a.getViewCount());
            row.put("点赞数", a.getLikeCount());
            row.put("创建时间", a.getCreateTime() != null ? a.getCreateTime().format(fmt) : "");
            result.add(row);
        }
        return Result.success(result);
    }

    @Operation(summary = "导出用户列表", description = "返回用户列表数据用于CSV导出")
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> exportUsers() {
        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
                        .orderByDesc(SysUser::getCreateTime)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SysUser u : users) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ID", u.getId());
            row.put("用户名", u.getUsername());
            row.put("昵称", u.getNickname());
            row.put("邮箱", u.getEmail());
            row.put("状态", u.getStatus() == 1 ? "正常" : "停用");
            row.put("创建时间", u.getCreateTime() != null ? u.getCreateTime().format(fmt) : "");
            result.add(row);
        }
        return Result.success(result);
    }
}
