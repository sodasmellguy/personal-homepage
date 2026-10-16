package com.example.grzy.service;

import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbAlbumGroup;
import com.example.grzy.entity.TbAlbumPhoto;

import java.util.List;

/**
 * 相册服务
 *
 * @author sodasmell
 */
public interface AlbumService {

    /**
     * 获取所有显示状态的照片（前台，按分组筛选，按排序+拍摄日期）
     */
    List<TbAlbumPhoto> getVisiblePhotos(Long groupId);

    /**
     * 获取所有显示状态的分组（前台）
     */
    List<TbAlbumGroup> getVisibleGroups();

    /**
     * 获取全部分组（后台管理）
     */
    List<TbAlbumGroup> getAllGroups();

    /**
     * 创建分组
     */
    void createGroup(TbAlbumGroup group);

    /**
     * 更新分组
     */
    void updateGroup(Long id, TbAlbumGroup group);

    /**
     * 删除分组（照片回归未分组）
     */
    void deleteGroup(Long id);

    /**
     * 分页查询照片列表（后台）
     */
    PageResult<TbAlbumPhoto> getPhotoPage(Integer pageNum, Integer pageSize, String title, Integer status, Long groupId);

    /**
     * 根据ID获取照片
     */
    TbAlbumPhoto getById(Long id);

    /**
     * 创建照片（自动解析上传图片的 EXIF 拍摄参数）
     *
     * @return 保存后的照片（含ID和EXIF字段）
     */
    TbAlbumPhoto createPhoto(TbAlbumPhoto photo);

    /**
     * 更新照片
     */
    void updatePhoto(Long id, TbAlbumPhoto photo);

    /**
     * 删除照片
     */
    void deletePhoto(Long id);
}
