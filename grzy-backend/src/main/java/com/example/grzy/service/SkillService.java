package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbSkill;
import com.example.grzy.vo.SkillVO;

import java.util.List;

/**
 * 技能服务接口
 *
 * @author sodasmell
 */
public interface SkillService extends IService<TbSkill> {

    /**
     * 分页查询技能列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param name     技能名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResult<SkillVO> getSkillPage(Integer pageNum, Integer pageSize, String name, Integer status);

    /**
     * 获取所有技能（前台展示）
     *
     * @return 技能列表
     */
    List<SkillVO> getAllSkills();

    /**
     * 创建技能
     *
     * @param skill 技能
     */
    void createSkill(TbSkill skill);

    /**
     * 更新技能
     *
     * @param skillId 技能ID
     * @param skill   技能
     */
    void updateSkill(Long skillId, TbSkill skill);

    /**
     * 删除技能
     *
     * @param skillId 技能ID
     */
    void deleteSkill(Long skillId);

    /**
     * 根据ID获取技能详情
     *
     * @param id 技能ID
     * @return 技能VO
     */
    SkillVO getSkillById(Long id);
}
