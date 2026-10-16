package com.example.grzy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访客位置记录实体
 *
 * @author sodasmell
 */
@Data
@TableName("tb_visitor_location")
public class TbVisitorLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 访客标识（IP+UA 的 MD5）
     */
    private String visitorId;

    /**
     * 访客 IP 地址
     */
    private String ip;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份/州
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 访问时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime visitTime;
}
