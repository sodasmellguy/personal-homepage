package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbAward;
import com.example.grzy.service.AwardService;
import com.example.grzy.vo.AwardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 获奖经历管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "获奖经历管理", description = "获奖经历增删改查接口")
@RestController
@RequestMapping("/api/award")
@RequiredArgsConstructor
@SaCheckPermission("content:award:view")
public class AwardController {

    private final AwardService awardService;

    /**
     * 分页查询获奖经历列表
     */
    @Operation(summary = "获奖经历列表", description = "分页查询获奖经历列表")
    @GetMapping("/list")
    public Result<PageResult<AwardVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<AwardVO> pageResult = awardService.getAwardPage(pageNum, pageSize, title, status);
        return Result.success(pageResult);
    }

    /**
     * 获取获奖经历详情
     */
    @Operation(summary = "获奖经历详情", description = "根据ID获取获奖经历详情")
    @GetMapping("/{id}")
    public Result<AwardVO> get(@PathVariable Long id) {
        AwardVO vo = awardService.getAwardById(id);
        return Result.success(vo);
    }

    /**
     * 创建获奖经历
     */
    @Operation(summary = "创建获奖经历", description = "创建新获奖经历")
    @PostMapping
    public Result<Void> create(@RequestBody TbAward award) {
        awardService.createAward(award);
        return Result.success();
    }

    /**
     * 更新获奖经历
     */
    @Operation(summary = "更新获奖经历", description = "根据ID更新获奖经历")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TbAward award) {
        awardService.updateAward(id, award);
        return Result.success();
    }

    /**
     * 删除获奖经历
     */
    @Operation(summary = "删除获奖经历", description = "根据ID删除获奖经历")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        awardService.deleteAward(id);
        return Result.success();
    }

    @Operation(summary = "批量删除获奖经历", description = "根据ID列表批量删除获奖经历")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        ids.forEach(awardService::deleteAward);
        return Result.success();
    }
}
