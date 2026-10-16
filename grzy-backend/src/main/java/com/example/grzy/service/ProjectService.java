package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbProject;
import com.example.grzy.vo.ProjectVO;

import java.util.List;

/**
 * 项目服务接口
 *
 * @author sodasmell
 */
public interface ProjectService extends IService<TbProject> {

    /**
     * 分页查询项目列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    项目名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResult<ProjectVO> getProjectPage(Integer pageNum, Integer pageSize, String title, Integer status);

    /**
     * 获取所有项目（前台展示）
     *
     * @return 项目列表
     */
    List<ProjectVO> getAllProjects();

    /**
     * 创建项目
     *
     * @param project 项目
     */
    void createProject(TbProject project);

    /**
     * 更新项目
     *
     * @param projectId 项目ID
     * @param project   项目
     */
    void updateProject(Long projectId, TbProject project);

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     */
    void deleteProject(Long projectId);

    /**
     * 根据ID获取项目详情
     *
     * @param id 项目ID
     * @return 项目VO
     */
    ProjectVO getProjectById(Long id);
}
