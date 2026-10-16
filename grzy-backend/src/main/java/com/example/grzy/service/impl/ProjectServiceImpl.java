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
import com.example.grzy.entity.TbProject;
import com.example.grzy.mapper.TbProjectMapper;
import com.example.grzy.service.OperLogService;
import com.example.grzy.service.ProjectService;
import com.example.grzy.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<TbProjectMapper, TbProject> implements ProjectService {

    private final TbProjectMapper projectMapper;
    private final OperLogService operLogService;

    @Override
    public PageResult<ProjectVO> getProjectPage(Integer pageNum, Integer pageSize, String title, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<TbProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TbProject::getDeleted, CommonConstant.NOT_DELETED);
        if (StringUtils.hasText(title)) {
            wrapper.like(TbProject::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(TbProject::getStatus, status);
        }
        wrapper.orderByAsc(TbProject::getSort);

        // 分页查询
        IPage<TbProject> page = new Page<>(pageNum, pageSize);
        IPage<TbProject> result = projectMapper.selectPage(page, wrapper);

        // 转换为 VO 列表
        List<ProjectVO> voList = result.getRecords().stream().map(project -> {
            ProjectVO vo = new ProjectVO();
            BeanUtil.copyProperties(project, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, pageNum, pageSize);
    }

    @Override
    public List<ProjectVO> getAllProjects() {
        // 查询所有启用的项目
        List<TbProject> projects = projectMapper.selectList(
                new LambdaQueryWrapper<TbProject>()
                        .eq(TbProject::getStatus, CommonConstant.STATUS_NORMAL)
                        .eq(TbProject::getDeleted, CommonConstant.NOT_DELETED)
                        .orderByAsc(TbProject::getSort)
        );

        // 转换为 VO
        return projects.stream().map(project -> {
            ProjectVO vo = new ProjectVO();
            BeanUtil.copyProperties(project, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ProjectVO getProjectById(Long id) {
        TbProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        ProjectVO vo = new ProjectVO();
        BeanUtil.copyProperties(project, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProject(TbProject project) {
        project.setStatus(project.getStatus() != null ? project.getStatus() : CommonConstant.STATUS_NORMAL);
        project.setSort(project.getSort() != null ? project.getSort() : 0);
        projectMapper.insert(project);
        log.info("创建项目成功: {}", project.getTitle());
        operLogService.log("项目管理", "创建了项目: " + project.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(Long projectId, TbProject project) {
        // 查询项目
        TbProject existing = projectMapper.selectById(projectId);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 更新项目
        project.setId(projectId);
        projectMapper.updateById(project);
        log.info("更新项目成功: {}", project.getTitle());
        operLogService.log("项目管理", "更新了项目: " + project.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        // 查询项目
        TbProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 逻辑删除
        projectMapper.deleteById(projectId);
        log.info("删除项目成功: {}", project.getTitle());
        operLogService.log("项目管理", "删除了项目: " + project.getTitle());
    }
}
