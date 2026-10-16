package com.example.grzy.service;

import com.example.grzy.entity.TbProfile;
import com.example.grzy.vo.ProfileVO;

/**
 * 个人信息服务接口
 *
 * @author sodasmell
 */
public interface ProfileService {

    /**
     * 获取个人信息
     *
     * @return 个人信息
     */
    ProfileVO getProfile();

    /**
     * 更新个人信息
     *
     * @param profile 个人信息
     */
    void updateProfile(TbProfile profile);
}
