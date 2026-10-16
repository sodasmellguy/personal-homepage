package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbSeries;
import com.example.grzy.service.SeriesService;
import com.example.grzy.vo.SeriesDetailVO;
import com.example.grzy.vo.SeriesVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专栏管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "专栏管理", description = "专栏增删改查接口")
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
@SaCheckPermission("blog:series:view")
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * 分页查询专栏列表
     */
    @Operation(summary = "专栏列表", description = "分页查询专栏列表")
    @GetMapping("/list")
    public Result<PageResult<SeriesVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<SeriesVO> pageResult = seriesService.getSeriesPage(pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 获取专栏详情
     */
    @Operation(summary = "专栏详情", description = "根据ID获取专栏详情")
    @GetMapping("/{id}")
    public Result<SeriesDetailVO> get(@PathVariable Long id) {
        SeriesDetailVO detail = seriesService.getSeriesDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建专栏
     */
    @Operation(summary = "创建专栏", description = "创建新专栏")
    @PostMapping
    public Result<Void> create(@RequestBody SeriesCreateDTO dto) {
        seriesService.createSeries(dto.toSeries(), dto.getArticleIds());
        return Result.success();
    }

    /**
     * 更新专栏
     */
    @Operation(summary = "更新专栏", description = "根据ID更新专栏")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SeriesCreateDTO dto) {
        seriesService.updateSeries(id, dto.toSeries(), dto.getArticleIds());
        return Result.success();
    }

    /**
     * 删除专栏
     */
    @Operation(summary = "删除专栏", description = "根据ID删除专栏")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        seriesService.deleteSeries(id);
        return Result.success();
    }

    /**
     * 专栏创建/更新请求体
     */
    @Data
    public static class SeriesCreateDTO {
        private String name;
        private String coverImage;
        private String description;
        private Integer sort;
        private Integer status;
        private List<Long> articleIds;

        public TbSeries toSeries() {
            TbSeries series = new TbSeries();
            series.setName(name);
            series.setCoverImage(coverImage);
            series.setDescription(description);
            series.setSort(sort);
            series.setStatus(status);
            return series;
        }
    }
}
