package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbSkill;
import com.example.grzy.service.SkillService;
import com.example.grzy.vo.SkillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "技能管理", description = "技能增删改查接口")
@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
@SaCheckPermission("content:skill:view")
public class SkillController {

    private final SkillService skillService;

    /**
     * 分页查询技能列表
     */
    @Operation(summary = "技能列表", description = "分页查询技能列表")
    @GetMapping("/list")
    public Result<PageResult<SkillVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<SkillVO> pageResult = skillService.getSkillPage(pageNum, pageSize, name, status);
        return Result.success(pageResult);
    }

    /**
     * 获取技能详情
     */
    @Operation(summary = "技能详情", description = "根据ID获取技能详情")
    @GetMapping("/{id}")
    public Result<SkillVO> get(@PathVariable Long id) {
        SkillVO vo = skillService.getSkillById(id);
        return Result.success(vo);
    }

    /**
     * 创建技能
     */
    @Operation(summary = "创建技能", description = "创建新技能")
    @PostMapping
    public Result<Void> create(@RequestBody TbSkill skill) {
        skillService.createSkill(skill);
        return Result.success();
    }

    /**
     * 更新技能
     */
    @Operation(summary = "更新技能", description = "根据ID更新技能")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbSkill skill) {
        skillService.updateSkill(id, skill);
        return Result.success();
    }

    /**
     * 删除技能
     */
    @Operation(summary = "删除技能", description = "根据ID删除技能")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return Result.success();
    }

    @Operation(summary = "批量删除技能", description = "根据ID列表批量删除技能")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(skillService::deleteSkill);
        return Result.success();
    }
}
