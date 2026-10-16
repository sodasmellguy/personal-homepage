package com.example.grzy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 友链申请 DTO
 *
 * @author sodasmell
 */
@Data
public class FriendLinkApplyDTO {

    /**
     * 站点名称
     */
    @NotBlank(message = "站点名称不能为空")
    @Size(max = 64, message = "站点名称不能超过64个字符")
    private String name;

    /**
     * 站点链接
     */
    @NotBlank(message = "站点链接不能为空")
    @Size(max = 255, message = "站点链接不能超过255个字符")
    private String url;

    /**
     * 站点图标/头像
     */
    @Size(max = 255, message = "图标地址不能超过255个字符")
    private String logo;

    /**
     * 站点描述
     */
    @Size(max = 255, message = "站点描述不能超过255个字符")
    private String description;
}
