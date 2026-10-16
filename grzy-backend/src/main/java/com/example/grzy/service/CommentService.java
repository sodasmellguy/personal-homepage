package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbComment;
import com.example.grzy.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author sodasmell
 */
public interface CommentService extends IService<TbComment> {

    /**
     * 分页查询评论列表（后台）
     */
    PageResult<CommentVO> getCommentPage(Integer pageNum, Integer pageSize, Long articleId, Integer status);

    /**
     * 获取文章的评论树（前台）
     */
    List<CommentVO> getArticleComments(Long articleId);

    /**
     * 提交评论
     */
    void createComment(TbComment comment);

    /**
     * 审核评论
     */
    void auditComment(Long commentId, Integer status);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);

    /**
     * 批量删除评论
     */
    void batchDeleteComments(List<Long> ids);
}
