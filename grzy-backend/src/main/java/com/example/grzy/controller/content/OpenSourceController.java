package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbOpenSource;
import com.example.grzy.service.OpenSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 开源项目管理控制器（后台，需要认证）
 *
 * @author sodasmell
 */
@Tag(name = "开源项目管理", description = "开源项目增删改查与仓库数据同步")
@RestController
@RequestMapping("/api/open-source")
@RequiredArgsConstructor
@SaCheckPermission("content:opensource:view")
public class OpenSourceController {

    private final OpenSourceService openSourceService;

    /**
     * 分页查询开源项目列表
     */
    @Operation(summary = "开源项目列表", description = "分页查询开源项目列表（后台）")
    @GetMapping("/list")
    public Result<PageResult<TbOpenSource>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(openSourceService.getAdminPage(pageNum, pageSize, status));
    }

    /**
     * 获取开源项目详情
     */
    @Operation(summary = "开源项目详情", description = "根据ID获取开源项目详情")
    @GetMapping("/{id}")
    public Result<TbOpenSource> get(@PathVariable Long id) {
        return Result.success(openSourceService.getById(id));
    }

    /**
     * 创建开源项目
     */
    @Operation(summary = "创建开源项目", description = "新增开源项目")
    @PostMapping
    public Result<Void> create(@RequestBody TbOpenSource item) {
        openSourceService.create(item);
        return Result.success();
    }

    /**
     * 更新开源项目
     */
    @Operation(summary = "更新开源项目", description = "根据ID更新开源项目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbOpenSource item) {
        openSourceService.update(id, item);
        return Result.success();
    }

    /**
     * 删除开源项目
     */
    @Operation(summary = "删除开源项目", description = "根据ID删除开源项目（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        openSourceService.delete(id);
        return Result.success();
    }

    /**
     * 同步仓库数据
     */
    @Operation(summary = "同步仓库数据", description = "从 Gitee/GitHub 公开 API 同步 star/fork/license")
    @PostMapping("/{id}/sync")
    public Result<TbOpenSource> sync(@PathVariable Long id) {
        return Result.success(openSourceService.syncFromRepo(id));
    }
}
