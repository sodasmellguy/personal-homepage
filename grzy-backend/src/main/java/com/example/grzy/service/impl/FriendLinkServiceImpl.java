package com.example.grzy.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.grzy.common.constant.CommonConstant;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.ResultCode;
import com.example.grzy.entity.TbFriendLink;
import com.example.grzy.mapper.TbFriendLinkMapper;
import com.example.grzy.service.FriendLinkService;
import com.example.grzy.service.NotifyService;
import com.example.grzy.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 友情链接服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendLinkServiceImpl extends ServiceImpl<TbFriendLinkMapper, TbFriendLink> implements FriendLinkService {

    /** 待审核 */
    public static final Integer STATUS_PENDING = 0;
    /** 已通过（对外显示） */
    public static final Integer STATUS_APPROVED = 1;
    /** 已驳回 */
    public static final Integer STATUS_REJECTED = 2;
    /** 已停用（审核通过后手动下架） */
    public static final Integer STATUS_DISABLED = 3;

    private final TbFriendLinkMapper friendLinkMapper;
    private final OperLogService operLogService;
    private final NotifyService notifyService;

    @Override
    public List<TbFriendLink> getApprovedLinks() {
        return friendLinkMapper.selectList(
                new LambdaQueryWrapper<TbFriendLink>()
                        .eq(TbFriendLink::getDeleted, CommonConstant.NOT_DELETED)
                        .eq(TbFriendLink::getStatus, STATUS_APPROVED)
                        .orderByAsc(TbFriendLink::getSort)
                        .orderByDesc(TbFriendLink::getCreateTime)
        );
    }

    @Override
    public PageResult<TbFriendLink> getLinkPage(Integer pageNum, Integer pageSize, String name, Integer status) {
        LambdaQueryWrapper<TbFriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbFriendLink::getDeleted, CommonConstant.NOT_DELETED);
        if (StrUtil.isNotBlank(name)) {
            wrapper.like(TbFriendLink::getName, name);
        }
        if (status != null) {
            wrapper.eq(TbFriendLink::getStatus, status);
        }
        wrapper.orderByAsc(TbFriendLink::getStatus)
                .orderByAsc(TbFriendLink::getSort)
                .orderByDesc(TbFriendLink::getCreateTime);

        IPage<TbFriendLink> page = new Page<>(pageNum, pageSize);
        IPage<TbFriendLink> result = friendLinkMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    @Override
    public TbFriendLink getById(Long id) {
        TbFriendLink link = friendLinkMapper.selectById(id);
        if (link == null || CommonConstant.DELETED.equals(link.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return link;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(TbFriendLink link) {
        link.setId(null);
        link.setStatus(STATUS_PENDING);
        link.setSort(0);
        friendLinkMapper.insert(link);
        log.info("收到友链申请: {} ({})", link.getName(), link.getUrl());
        // 异步通知站长有新申请（NotifyService 内部吞异常，不影响申请主流程）
        notifyService.notifyFriendLinkApply(link.getName(), link.getUrl(), link.getDescription());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TbFriendLink link) {
        link.setId(null);
        // 统一默认待审核，必须显式「通过」才对外显示
        link.setStatus(STATUS_PENDING);
        link.setSort(link.getSort() != null ? link.getSort() : 0);
        friendLinkMapper.insert(link);
        operLogService.log("友链管理", "创建了友链(待审核): " + link.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TbFriendLink link) {
        TbFriendLink existing = friendLinkMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        link.setId(id);
        friendLinkMapper.updateById(link);
        operLogService.log("友链管理", "更新了友链: " + link.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TbFriendLink existing = friendLinkMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        friendLinkMapper.deleteById(id);
        operLogService.log("友链管理", "删除了友链: " + existing.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)
                && !STATUS_PENDING.equals(status) && !STATUS_DISABLED.equals(status)) {
            throw new BusinessException("非法的状态值");
        }
        TbFriendLink existing = friendLinkMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        TbFriendLink link = new TbFriendLink();
        link.setId(id);
        link.setStatus(status);
        friendLinkMapper.updateById(link);
        String action;
        if (STATUS_APPROVED.equals(status)) {
            action = "通过了友链: ";
        } else if (STATUS_REJECTED.equals(status)) {
            action = "驳回了友链申请: ";
        } else if (STATUS_DISABLED.equals(status)) {
            action = "停用了友链: ";
        } else {
            action = "将友链转为待审核: ";
        }
        operLogService.log("友链管理", action + existing.getName());
    }
}
