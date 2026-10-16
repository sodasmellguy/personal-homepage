package com.example.grzy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbComment;
import com.example.grzy.mapper.TbCommentMapper;
import com.example.grzy.service.CommentService;
import com.example.grzy.service.OperLogService;
import com.example.grzy.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<TbCommentMapper, TbComment> implements CommentService {

    private final TbCommentMapper commentMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<CommentVO> getCommentPage(Integer pageNum, Integer pageSize, Long articleId, Integer status) {
        LambdaQueryWrapper<TbComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbComment::getDeleted, CommonConstant.NOT_DELETED);
        if (articleId != null) {
            wrapper.eq(TbComment::getArticleId, articleId);
        }
        if (status != null) {
            wrapper.eq(TbComment::getStatus, status);
        }
        wrapper.orderByDesc(TbComment::getCreateTime);

        IPage<TbComment> page = new Page<>(pageNum, pageSize);
        IPage<TbComment> result = commentMapper.selectPage(page, wrapper);

        List<CommentVO> voList = result.getRecords().stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtil.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<CommentVO> getArticleComments(Long articleId) {
        List<TbComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<TbComment>()
                        .eq(TbComment::getArticleId, articleId)
                        .eq(TbComment::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbComment::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbComment::getCreateTime)
        );
        return buildCommentTree(comments, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(TbComment comment) {
        comment.setParentId(comment.getParentId() != null ? comment.getParentId() : 0L);
        comment.setStatus(CommonConstant.STATUS_NORMAL);
        commentMapper.insert(comment);
        log.info("新增评论成功: 文章ID={}, 昵称={}", comment.getArticleId(), comment.getNickname());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditComment(Long commentId, Integer status) {
        TbComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);
        log.info("审核评论成功: ID={}, 状态={}", commentId, status);
        operLogService.log("评论管理", "审核了评论: ID=" + commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        TbComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        commentMapper.deleteById(commentId);
        log.info("删除评论成功: ID={}", commentId);
        operLogService.log("评论管理", "删除了评论: ID=" + commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteComments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        commentMapper.deleteBatchIds(ids);
        log.info("批量删除评论成功: 共{}条", ids.size());
        operLogService.log("评论管理", "批量删除了评论: 共" + ids.size() + "条");
    }

    /**
     * 构建评论树
     */
    private List<CommentVO> buildCommentTree(List<TbComment> allComments, Long parentId) {
        return allComments.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(c -> {
                    CommentVO vo = new CommentVO();
                    BeanUtil.copyProperties(c, vo);
                    // 安全：评论树仅用于前台公开接口，置空邮箱防止访客邮箱泄露（后台分页列表单独组装、保留邮箱）
                    vo.setEmail(null);
                    vo.setChildren(buildCommentTree(allComments, c.getId()));
                    if (c.getReplyToId() != null && c.getReplyToId() > 0) {
                        allComments.stream()
                                .filter(reply -> reply.getId().equals(c.getReplyToId()))
                                .findFirst()
                                .ifPresent(reply -> vo.setReplyToNickname(reply.getNickname()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
