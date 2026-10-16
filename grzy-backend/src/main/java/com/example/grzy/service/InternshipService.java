package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbInternship;
import com.example.grzy.vo.InternshipVO;

import java.util.List;

/**
 * 实习经历服务接口
 *
 * @author sodasmell
 */
public interface InternshipService extends IService<TbInternship> {

    /**
     * 分页查询实习经历列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param company  公司名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResult<InternshipVO> getInternshipPage(Integer pageNum, Integer pageSize, String company, Integer status);

    /**
     * 获取所有实习经历（前台展示）
     *
     * @return 实习经历列表
     */
    List<InternshipVO> getAllInternships();

    /**
     * 创建实习经历
     *
     * @param internship 实习经历
     */
    void createInternship(TbInternship internship);

    /**
     * 更新实习经历
     *
     * @param internshipId 实习经历ID
     * @param internship   实习经历
     */
    void updateInternship(Long internshipId, TbInternship internship);

    /**
     * 删除实习经历
     *
     * @param internshipId 实习经历ID
     */
    void deleteInternship(Long internshipId);

    /**
     * 根据ID获取实习经历详情
     *
     * @param id 实习经历ID
     * @return 实习经历VO
     */
    InternshipVO getInternshipById(Long id);
}
