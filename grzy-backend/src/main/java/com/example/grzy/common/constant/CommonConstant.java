package com.example.grzy.common.constant;

/**
 * 通用常量
 *
 * @author sodasmell
 */
public class CommonConstant {

    /**
     * 正常状态
     */
    public static final Integer STATUS_NORMAL = 1;

    /**
     * 禁用状态
     */
    public static final Integer STATUS_DISABLED = 0;

    /**
     * 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 默认页码
     */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;

    /**
     * 超级管理员角色编码
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * 普通用户角色编码
     */
    public static final String ROLE_USER = "user";

    /**
     * 菜单类型 - 目录
     */
    public static final Integer MENU_TYPE_DIR = 0;

    /**
     * 菜单类型 - 菜单
     */
    public static final Integer MENU_TYPE_MENU = 1;

    /**
     * 菜单类型 - 按钮
     */
    public static final Integer MENU_TYPE_BUTTON = 2;

    /**
     * 限制分页大小在合法范围内
     */
    public static int clampPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 限制页码在合法范围内
     */
    public static int clampPageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }
}
