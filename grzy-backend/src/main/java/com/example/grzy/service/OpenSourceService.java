package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbOpenSource;

import java.util.List;

/**
 * 开源项目服务接口
 *
 * @author sodasmell
 */
public interface OpenSourceService extends IService<TbOpenSource> {

    /**
     * 前台列表（置顶优先 → sort 升序 → 创建时间倒序）
     */
    List<TbOpenSource> getFrontList();

    /**
     * 后台分页列表（可按状态筛选）
     */
    PageResult<TbOpenSource> getAdminPage(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 新增开源项目
     */
    void create(TbOpenSource item);

    /**
     * 更新开源项目
     */
    void update(Long id, TbOpenSource item);

    /**
     * 删除开源项目（逻辑删除）
     */
    void delete(Long id);

    /**
     * 从 Gitee/GitHub 公开 API 同步 star/fork/license
     *
     * @return 同步后的实体
     */
    TbOpenSource syncFromRepo(Long id);
}
