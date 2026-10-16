package com.example.grzy.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 在线会话视图对象
 *
 * @author sodasmell
 */
@Data
@Builder
public class OnlineSessionVO {

    /** 脱敏后的 Token（前 8 位） */
    private String token;

    /** 用户 ID（踢下线用） */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 登录 IP */
    private String ip;

    /** 浏览器 User-Agent */
    private String userAgent;

    /** 登录时间 */
    private String loginTime;

    /** 是否为当前会话 */
    private Boolean current;
}
