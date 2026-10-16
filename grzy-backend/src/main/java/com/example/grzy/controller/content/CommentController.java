package com.example.grzy.controller.content;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.service.CommentService;
import com.example.grzy.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 *
 * @author sodasmell
 */
@Tag(name = "评论管理", description = "评论管理接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@SaCheckPermission("blog:comment:view")
public class CommentController {

    private final CommentService commentService;

    /**
     * 分页查询评论列表
     */
    @Operation(summary = "评论列表", description = "分页查询评论列表")
    @GetMapping("/list")
    public Result<PageResult<CommentVO>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "articleId", required = false) Long articleId,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<CommentVO> pageResult = commentService.getCommentPage(pageNum, pageSize, articleId, status);
        return Result.success(pageResult);
    }

    /**
     * 审核评论
     */
    @Operation(summary = "审核评论", description = "修改评论状态")
    @PutMapping("/{id}/status")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        commentService.auditComment(id, status);
        return Result.success();
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "根据ID删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }

    @Operation(summary = "批量删除评论", description = "根据ID列表批量删除评论")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        commentService.batchDeleteComments(ids);
        return Result.success();
    }

    @Operation(summary = "批量审核评论", description = "根据ID列表批量审核评论")
    @PutMapping("/batch/status")
    public Result<Void> batchAudit(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.get("ids");
        Integer status = (Integer) body.get("status");
        if (ids != null) {
            ids.forEach(id -> commentService.auditComment(id.longValue(), status));
        }
        return Result.success();
    }
}
