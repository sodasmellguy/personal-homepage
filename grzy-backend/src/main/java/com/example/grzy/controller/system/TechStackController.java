package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbTechStack;
import com.example.grzy.service.TechStackService;
import com.example.grzy.vo.TechStackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技术栈管理控制器（后台管理）
 *
 * @author sodasmell
 */
@Tag(name = "技术栈管理", description = "技术栈增删改查接口")
@RestController
@RequestMapping("/api/techstack")
@RequiredArgsConstructor
@SaCheckPermission("content:techstack:view")
public class TechStackController {

    private final TechStackService techStackService;

    /**
     * 分页查询技术栈列表
     */
    @Operation(summary = "技术栈列表", description = "分页查询技术栈列表")
    @GetMapping("/list")
    public Result<PageResult<TbTechStack>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name) {
        PageResult<TbTechStack> pageResult = techStackService.getTechStackPage(pageNum, pageSize, name);
        return Result.success(pageResult);
    }

    /**
     * 获取技术栈详情
     */
    @Operation(summary = "技术栈详情", description = "根据ID获取技术栈详情")
    @GetMapping("/{id}")
    public Result<TbTechStack> get(@PathVariable Long id) {
        TbTechStack techStack = techStackService.getTechStackById(id);
        return Result.success(techStack);
    }

    /**
     * 创建技术栈
     */
    @Operation(summary = "创建技术栈", description = "创建新技术栈")
    @PostMapping
    public Result<Void> create(@RequestBody TbTechStack techStack) {
        techStackService.createTechStack(techStack);
        return Result.success();
    }

    /**
     * 更新技术栈
     */
    @Operation(summary = "更新技术栈", description = "根据ID更新技术栈")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbTechStack techStack) {
        techStackService.updateTechStack(id, techStack);
        return Result.success();
    }

    /**
     * 删除技术栈
     */
    @Operation(summary = "删除技术栈", description = "根据ID删除技术栈")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        techStackService.deleteTechStack(id);
        return Result.success();
    }

    /**
     * 批量删除技术栈
     */
    @Operation(summary = "批量删除技术栈", description = "根据ID列表批量删除技术栈")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        techStackService.batchDeleteTechStacks(ids);
        return Result.success();
    }
}
