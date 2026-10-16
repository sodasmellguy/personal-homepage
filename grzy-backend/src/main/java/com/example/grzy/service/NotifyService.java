package com.example.grzy.service;

import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbNotifyLog;
import com.example.grzy.entity.TbNotifySetting;

/**
 * 邮件通知服务 — 站点各类事件的邮件通知统一入口
 * 含开关检查、频控、异步发送（失败重试 1 次）与通知日志留痕
 *
 * @author sodasmell
 */
public interface NotifyService {

    /**
     * 新评论通知站长
     *
     * @param articleTitle 文章标题
     * @param nickname     评论者昵称
     * @param content      评论内容
     */
    void notifyNewComment(String articleTitle, String nickname, String content);

    /**
     * 评论回复通知被回复的访客
     */
    void notifyReply(String toEmail, String replierName, String replyContent, String articleTitle);

    /**
     * 友链申请通知站长
     */
    void notifyFriendLinkApply(String name, String url, String description);

    /**
     * 定时发布结果通知站长
     *
     * @param success  是否发布成功
     * @param title    文章标题
     * @param errorMsg 失败原因（成功时为 null）
     */
    void notifyPublishResult(boolean success, String title, String errorMsg);

    /**
     * 安全告警通知站长（新 IP 登录等，带频控防轰炸）
     *
     * @param username 登录账号
     * @param ip       登录 IP
     * @param event    事件描述
     */
    void notifySecurityAlert(String username, String ip, String event);

    /**
     * 获取通知设置（单行，不存在则自动初始化默认行）
     */
    TbNotifySetting getSetting();

    /**
     * 更新通知设置
     */
    void updateSetting(TbNotifySetting setting);

    /**
     * 分页查询通知日志
     */
    PageResult<TbNotifyLog> getLogPage(Integer pageNum, Integer pageSize, String type, Integer status);
}
