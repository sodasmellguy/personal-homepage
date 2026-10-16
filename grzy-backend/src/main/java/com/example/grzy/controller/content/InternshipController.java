package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbInternship;
import com.example.grzy.service.InternshipService;
import com.example.grzy.vo.InternshipVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 实习经历管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "实习经历管理", description = "实习经历增删改查接口")
@RestController
@RequestMapping("/api/internship")
@RequiredArgsConstructor
@SaCheckPermission("content:internship:view")
public class InternshipController {

    private final InternshipService internshipService;

    /**
     * 分页查询实习经历列表
     */
    @Operation(summary = "实习经历列表", description = "分页查询实习经历列表")
    @GetMapping("/list")
    public Result<PageResult<InternshipVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<InternshipVO> pageResult = internshipService.getInternshipPage(pageNum, pageSize, company, status);
        return Result.success(pageResult);
    }

    /**
     * 获取实习经历详情
     */
    @Operation(summary = "实习经历详情", description = "根据ID获取实习经历详情")
    @GetMapping("/{id}")
    public Result<InternshipVO> get(@PathVariable Long id) {
        InternshipVO vo = internshipService.getInternshipById(id);
        return Result.success(vo);
    }

    /**
     * 创建实习经历
     */
    @Operation(summary = "创建实习经历", description = "创建新实习经历")
    @PostMapping
    public Result<Void> create(@RequestBody TbInternship internship) {
        internshipService.createInternship(internship);
        return Result.success();
    }

    /**
     * 更新实习经历
     */
    @Operation(summary = "更新实习经历", description = "根据ID更新实习经历")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbInternship internship) {
        internshipService.updateInternship(id, internship);
        return Result.success();
    }

    /**
     * 删除实习经历
     */
    @Operation(summary = "删除实习经历", description = "根据ID删除实习经历")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        internshipService.deleteInternship(id);
        return Result.success();
    }
}
