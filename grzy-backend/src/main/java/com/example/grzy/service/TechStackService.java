package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbTechStack;
import com.example.grzy.vo.TechStackVO;

import java.util.List;

/**
 * 技术栈服务接口
 *
 * @author sodasmell
 */
public interface TechStackService extends IService<TbTechStack> {

    /**
     * 分页查询技术栈列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param name     技术名称（可选）
     * @return 分页结果
     */
    PageResult<TbTechStack> getTechStackPage(Integer pageNum, Integer pageSize, String name);

    /**
     * 获取所有技术栈（前台展示）
     *
     * @return 技术栈VO列表
     */
    List<TechStackVO> getAllTechStacks();

    /**
     * 根据ID获取技术栈详情
     *
     * @param id 技术栈ID
     * @return 技术栈实体
     */
    TbTechStack getTechStackById(Long id);

    /**
     * 创建技术栈
     *
     * @param techStack 技术栈
     */
    void createTechStack(TbTechStack techStack);

    /**
     * 更新技术栈
     *
     * @param id        技术栈ID
     * @param techStack 技术栈
     */
    void updateTechStack(Long id, TbTechStack techStack);

    /**
     * 删除技术栈
     *
     * @param id 技术栈ID
     */
    void deleteTechStack(Long id);

    /**
     * 批量删除技术栈
     *
     * @param ids 技术栈ID列表
     */
    void batchDeleteTechStacks(List<Long> ids);
}
