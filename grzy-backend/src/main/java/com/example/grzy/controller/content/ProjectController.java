package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbProject;
import com.example.grzy.service.ProjectService;
import com.example.grzy.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "项目管理", description = "项目增删改查接口")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@SaCheckPermission("content:project:view")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页查询项目列表
     */
    @Operation(summary = "项目列表", description = "分页查询项目列表")
    @GetMapping("/list")
    public Result<PageResult<ProjectVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<ProjectVO> pageResult = projectService.getProjectPage(pageNum, pageSize, title, status);
        return Result.success(pageResult);
    }

    /**
     * 获取项目详情
     */
    @Operation(summary = "项目详情", description = "根据ID获取项目详情")
    @GetMapping("/{id}")
    public Result<ProjectVO> get(@PathVariable Long id) {
        ProjectVO vo = projectService.getProjectById(id);
        return Result.success(vo);
    }

    /**
     * 创建项目
     */
    @Operation(summary = "创建项目", description = "创建新项目")
    @PostMapping
    public Result<Void> create(@RequestBody TbProject project) {
        projectService.createProject(project);
        return Result.success();
    }

    /**
     * 更新项目
     */
    @Operation(summary = "更新项目", description = "根据ID更新项目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbProject project) {
        projectService.updateProject(id, project);
        return Result.success();
    }

    /**
     * 删除项目
     */
    @Operation(summary = "删除项目", description = "根据ID删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.success();
    }

    @Operation(summary = "批量删除项目", description = "根据ID列表批量删除项目")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(projectService::deleteProject);
        return Result.success();
    }
}
