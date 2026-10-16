package com.example.grzy.service;

import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbFriendLink;

import java.util.List;

/**
 * 友情链接服务
 *
 * @author sodasmell
 */
public interface FriendLinkService {

    /**
     * 获取已通过的友链列表（前台展示，按排序）
     */
    List<TbFriendLink> getApprovedLinks();

    /**
     * 分页查询友链列表（后台管理）
     */
    PageResult<TbFriendLink> getLinkPage(Integer pageNum, Integer pageSize, String name, Integer status);

    /**
     * 根据ID获取友链
     */
    TbFriendLink getById(Long id);

    /**
     * 访客申请友链（默认待审核）
     */
    void apply(TbFriendLink link);

    /**
     * 创建友链（后台直接创建，默认已通过）
     */
    void create(TbFriendLink link);

    /**
     * 更新友链
     */
    void update(Long id, TbFriendLink link);

    /**
     * 删除友链
     */
    void delete(Long id);

    /**
     * 审核友链：1-通过，2-驳回
     */
    void updateStatus(Long id, Integer status);
}
