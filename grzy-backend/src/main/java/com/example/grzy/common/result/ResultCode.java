package com.example.grzy.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author sodasmell
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 操作失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数校验失败
     */
    VALID_ERROR(400, "参数校验失败"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "暂无权限"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1001, "用户名或密码错误"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(1002, "用户已被禁用"),

    /**
     * 账号已被锁定
     */
    USER_LOCKED(1003, "账号已被锁定"),

    /**
     * Token 已过期
     */
    TOKEN_EXPIRED(1004, "Token 已过期，请重新登录"),

    /**
     * Token 无效
     */
    TOKEN_INVALID(1005, "Token 无效"),

    /**
     * 数据已存在
     */
    DATA_EXIST(2001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXIST(2002, "数据不存在");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;
}
