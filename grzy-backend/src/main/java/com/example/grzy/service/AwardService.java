package com.example.grzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbAward;
import com.example.grzy.vo.AwardVO;

import java.util.List;

/**
 * 获奖经历服务接口
 *
 * @author sodasmell
 */
public interface AwardService extends IService<TbAward> {

    /**
     * 分页查询获奖经历列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    奖项名称（可选）
     * @param status   状态（可选）
     * @return 分页结果
     */
    PageResult<AwardVO> getAwardPage(Integer pageNum, Integer pageSize, String title, Integer status);

    /**
     * 获取所有获奖经历（前台展示）
     *
     * @return 获奖经历列表
     */
    List<AwardVO> getAllAwards();

    /**
     * 创建获奖经历
     *
     * @param award 获奖经历
     */
    void createAward(TbAward award);

    /**
     * 更新获奖经历
     *
     * @param awardId 获奖经历ID
     * @param award   获奖经历
     */
    void updateAward(Long awardId, TbAward award);

    /**
     * 删除获奖经历
     *
     * @param awardId 获奖经历ID
     */
    void deleteAward(Long awardId);

    /**
     * 根据ID获取获奖经历详情
     *
     * @param id 获奖经历ID
     * @return 获奖经历VO
     */
    AwardVO getAwardById(Long id);
}
