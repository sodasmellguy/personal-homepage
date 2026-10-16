package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.dto.ArticleCreateDTO;
import com.example.grzy.entity.TbArticle;
import com.example.grzy.service.ArticleService;
import com.example.grzy.vo.ArticleDetailVO;
import com.example.grzy.vo.ArticleVO;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "文章管理", description = "文章增删改查接口")
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@SaCheckPermission("blog:article:view")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 分页查询文章列表
     */
    @Operation(summary = "文章列表", description = "分页查询文章列表")
    @GetMapping("/list")
    public Result<PageResult<ArticleVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "category", required = false) String category) {
        pageNum = CommonConstant.clampPageNum(pageNum);
        pageSize = CommonConstant.clampPageSize(pageSize);
        PageResult<ArticleVO> pageResult = articleService.getArticlePage(pageNum, pageSize, title, status, category);
        return Result.success(pageResult);
    }

    /**
     * 获取文章详情
     */
    @Operation(summary = "文章详情", description = "根据ID获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> get(@PathVariable Long id) {
        ArticleDetailVO detail = articleService.getArticleDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建文章
     */
    @Operation(summary = "创建文章", description = "创建新文章")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ArticleCreateDTO dto) {
        TbArticle article = new TbArticle();
        BeanUtil.copyProperties(dto, article);
        articleService.createArticle(article);
        return Result.success();
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新文章", description = "根据ID更新文章")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleCreateDTO dto) {
        TbArticle article = new TbArticle();
        BeanUtil.copyProperties(dto, article);
        articleService.updateArticle(id, article);
        return Result.success();
    }

    /**
     * 删除文章
     */
    @Operation(summary = "删除文章", description = "根据ID删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
}
