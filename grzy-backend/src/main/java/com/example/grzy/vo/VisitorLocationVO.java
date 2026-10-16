package com.example.grzy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访客位置VO（前台公开）
 * 安全：刻意不含 ip 与 visitorId，防止访客真实 IP 等可识别信息对外泄露
 *
 * @author sodasmell
 */
@Data
@Schema(description = "访客位置数据（脱敏）")
public class VisitorLocationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "访问时间")
    private LocalDateTime visitTime;
}
