package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbFriendLink;
import com.example.grzy.service.FriendLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 友情链接管理控制器（后台，需要认证）
 *
 * @author sodasmell
 */
@Tag(name = "友情链接管理", description = "友链增删改查与审核接口")
@RestController
@RequestMapping("/api/friendlink")
@RequiredArgsConstructor
@SaCheckPermission("blog:friendlink:view")
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    /**
     * 分页查询友链列表
     */
    @Operation(summary = "友链列表", description = "分页查询友链列表（后台）")
    @GetMapping("/list")
    public Result<PageResult<TbFriendLink>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(friendLinkService.getLinkPage(pageNum, pageSize, name, status));
    }

    /**
     * 获取友链详情
     */
    @Operation(summary = "友链详情", description = "根据ID获取友链详情")
    @GetMapping("/{id}")
    public Result<TbFriendLink> get(@PathVariable Long id) {
        return Result.success(friendLinkService.getById(id));
    }

    /**
     * 创建友链
     */
    @Operation(summary = "创建友链", description = "后台直接创建友链（默认已通过）")
    @PostMapping
    public Result<Void> create(@RequestBody TbFriendLink link) {
        friendLinkService.create(link);
        return Result.success();
    }

    /**
     * 更新友链
     */
    @Operation(summary = "更新友链", description = "根据ID更新友链")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbFriendLink link) {
        friendLinkService.update(id, link);
        return Result.success();
    }

    /**
     * 审核友链
     */
    @Operation(summary = "审核友链", description = "通过或驳回友链申请：1-通过，2-驳回")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        friendLinkService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除友链
     */
    @Operation(summary = "删除友链", description = "根据ID删除友链")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        friendLinkService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除友链
     */
    @Operation(summary = "批量删除友链", description = "根据ID列表批量删除友链")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(friendLinkService::delete);
        return Result.success();
    }
}
