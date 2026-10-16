package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 相册照片实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_album_photo")
public class TbAlbumPhoto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 照片标题
     */
    private String title;

    /**
     * 照片描述/故事
     */
    private String description;

    /**
     * 图片地址
     */
    private String image;

    /**
     * 所属分组ID（NULL为未分组）
     */
    private Long groupId;

    /**
     * 拍摄地点
     */
    private String location;

    /**
     * 相机型号（EXIF解析）
     */
    private String camera;

    /**
     * 光圈，如 f/2.8（EXIF解析）
     */
    private String aperture;

    /**
     * 快门速度，如 1/250s（EXIF解析）
     */
    private String shutter;

    /**
     * 感光度，如 400（EXIF解析）
     */
    private String iso;

    /**
     * 焦距，如 35mm（EXIF解析）
     */
    private String focalLength;

    /**
     * 拍摄日期
     */
    private LocalDate shootDate;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
