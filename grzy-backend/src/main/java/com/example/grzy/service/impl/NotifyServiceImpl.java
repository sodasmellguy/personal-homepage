package com.example.grzy.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.entity.TbNotifyLog;
import com.example.grzy.entity.TbNotifySetting;
import com.example.grzy.mapper.TbNotifyLogMapper;
import com.example.grzy.mapper.TbNotifySettingMapper;
import com.example.grzy.service.NotifyService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 邮件通知服务实现
 * 流程：开关检查 → 频控（安全类告警）→ 异步发送（失败重试 1 次）→ 写通知日志
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    private final JavaMailSender mailSender;
    private final TbNotifyLogMapper notifyLogMapper;
    private final TbNotifySettingMapper notifySettingMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.admin-email}")
    private String defaultAdminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.site-url:http://localhost:8080}")
    private String siteUrl;

    /** 通知日志类型常量（与 tb_notify_log.type 一致） */
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_REPLY = "reply";
    public static final String TYPE_FRIENDLINK = "friendlink";
    public static final String TYPE_PUBLISH = "publish";
    public static final String TYPE_SECURITY = "security";

    /** 频控 Redis Key 前缀 */
    private static final String RATE_LIMIT_KEY = "grzy:notify:limit:";

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 业务通知入口（异步） ====================

    @Override
    @Async
    public void notifyNewComment(String articleTitle, String nickname, String content) {
        TbNotifySetting setting = getSetting();
        if (!isEnabled(setting.getEnableCommentNotify())) {
            return;
        }
        String subject = "博客收到新评论 - " + articleTitle;
        String html = String.format("""
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb; border-radius: 12px;">
              <h2 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px;">新评论通知</h2>
              <p style="color: #374151; margin: 16px 0;"><strong>文章：</strong>%s</p>
              <p style="color: #374151; margin: 16px 0;"><strong>评论者：</strong>%s</p>
              <div style="background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; margin: 16px 0;">
                <p style="color: #6b7280; margin: 0 0 8px; font-size: 12px;">评论内容：</p>
                <p style="color: #1f2937; margin: 0; line-height: 1.6;">%s</p>
              </div>
              <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">来自赵先生个人博客系统</p>
            </div>
            """, articleTitle, nickname, content);
        sendAndLog(TYPE_COMMENT, adminRecipient(setting), subject, html);
    }

    @Override
    @Async
    public void notifyReply(String toEmail, String replierName, String replyContent, String articleTitle) {
        TbNotifySetting setting = getSetting();
        if (!isEnabled(setting.getEnableReplyNotify())) {
            return;
        }
        if (StrUtil.isBlank(toEmail)) {
            return;
        }
        String subject = "您的评论收到了新回复";
        String html = String.format("""
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb; border-radius: 12px;">
              <h2 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px;">评论回复通知</h2>
              <p style="color: #374151; margin: 16px 0;">您的评论收到了 <strong>%s</strong> 的回复：</p>
              <div style="background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; margin: 16px 0;">
                <p style="color: #1f2937; margin: 0; line-height: 1.6;">%s</p>
              </div>
              <a href="%s/article" style="display: inline-block; padding: 10px 20px; background: #3b82f6; color: white; text-decoration: none; border-radius: 8px; margin-top: 12px;">查看回复</a>
              <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">来自赵先生个人博客</p>
            </div>
            """, replierName, replyContent, siteUrl);
        sendAndLog(TYPE_REPLY, toEmail, subject, html);
    }

    @Override
    @Async
    public void notifyFriendLinkApply(String name, String url, String description) {
        TbNotifySetting setting = getSetting();
        if (!isEnabled(setting.getEnableFriendlinkNotify())) {
            return;
        }
        String subject = "收到新的友链申请 - " + name;
        String html = String.format("""
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb; border-radius: 12px;">
              <h2 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px;">友链申请通知</h2>
              <p style="color: #374151; margin: 16px 0;"><strong>站点名称：</strong>%s</p>
              <p style="color: #374151; margin: 16px 0;"><strong>站点地址：</strong><a href="%s">%s</a></p>
              <div style="background: white; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; margin: 16px 0;">
                <p style="color: #6b7280; margin: 0 0 8px; font-size: 12px;">站点描述：</p>
                <p style="color: #1f2937; margin: 0; line-height: 1.6;">%s</p>
              </div>
              <a href="%s/admin/friendlink" style="display: inline-block; padding: 10px 20px; background: #3b82f6; color: white; text-decoration: none; border-radius: 8px; margin-top: 12px;">前往审核</a>
              <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">来自赵先生个人博客系统</p>
            </div>
            """, name, url, url, description != null ? description : "（无）", siteUrl);
        sendAndLog(TYPE_FRIENDLINK, adminRecipient(setting), subject, html);
    }

    @Override
    @Async
    public void notifyPublishResult(boolean success, String title, String errorMsg) {
        TbNotifySetting setting = getSetting();
        if (!isEnabled(setting.getEnablePublishNotify())) {
            return;
        }
        // 同频控：短时间内大量发布/失败只报一次，防轰炸
        if (!tryAcquireRateLimit(setting, TYPE_PUBLISH)) {
            return;
        }
        String subject = success ? "定时发布成功 - " + title : "定时发布失败 - " + title;
        String statusColor = success ? "#16a34a" : "#dc2626";
        String statusText = success ? "发布成功" : "发布失败";
        String html = String.format("""
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb; border-radius: 12px;">
              <h2 style="color: #1f2937; border-bottom: 2px solid #e5e7eb; padding-bottom: 12px;">定时发布通知</h2>
              <p style="color: #374151; margin: 16px 0;"><strong>文章：</strong>%s</p>
              <p style="color: #374151; margin: 16px 0;"><strong>结果：</strong><span style="color: %s; font-weight: bold;">%s</span></p>
              %s
              <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">来自赵先生个人博客系统</p>
            </div>
            """, title, statusColor, statusText,
                success ? "" : "<div style=\"background: white; border: 1px solid #fecaca; border-radius: 8px; padding: 16px; margin: 16px 0;\"><p style=\"color: #dc2626; margin: 0; line-height: 1.6;\">" + errorMsg + "</p></div>");
        sendAndLog(TYPE_PUBLISH, adminRecipient(setting), subject, html);
    }

    @Override
    @Async
    public void notifySecurityAlert(String username, String ip, String event) {
        TbNotifySetting setting = getSetting();
        if (!isEnabled(setting.getEnableSecurityNotify())) {
            return;
        }
        // 频控：同类告警 N 分钟内只发一条，防止攻击者利用登录接口刷邮件轰炸站长
        if (!tryAcquireRateLimit(setting, TYPE_SECURITY)) {
            log.info("安全告警触发频控，跳过发送: user={}, ip={}", username, ip);
            return;
        }
        String subject = "安全告警 - 账号异常登录提醒";
        String html = String.format("""
            <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb; border-radius: 12px;">
              <h2 style="color: #dc2626; border-bottom: 2px solid #fecaca; padding-bottom: 12px;">⚠ 安全告警</h2>
              <p style="color: #374151; margin: 16px 0;"><strong>账号：</strong>%s</p>
              <p style="color: #374151; margin: 16px 0;"><strong>登录 IP：</strong>%s</p>
              <p style="color: #374151; margin: 16px 0;"><strong>时间：</strong>%s</p>
              <div style="background: white; border: 1px solid #fecaca; border-radius: 8px; padding: 16px; margin: 16px 0;">
                <p style="color: #1f2937; margin: 0; line-height: 1.6;">%s</p>
              </div>
              <p style="color: #6b7280; font-size: 13px; margin-top: 12px;">若非本人操作，请立即修改密码并在后台「在线会话」中踢出可疑会话。</p>
              <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">来自赵先生个人博客系统</p>
            </div>
            """, username, ip, LocalDateTime.now().format(TIME_FMT), event);
        sendAndLog(TYPE_SECURITY, adminRecipient(setting), subject, html);
    }

    // ==================== 设置管理 ====================

    @Override
    public TbNotifySetting getSetting() {
        TbNotifySetting setting = notifySettingMapper.selectById(1L);
        if (setting != null) {
            return setting;
        }
        // 首读自动初始化默认行（全部开启，30 分钟频控）
        TbNotifySetting init = new TbNotifySetting();
        init.setId(1L);
        init.setEnableCommentNotify(1);
        init.setEnableReplyNotify(1);
        init.setEnableFriendlinkNotify(1);
        init.setEnablePublishNotify(1);
        init.setEnableSecurityNotify(1);
        init.setAlertIntervalMinutes(30);
        try {
            notifySettingMapper.insert(init);
        } catch (Exception e) {
            // 并发初始化时主键冲突，读已有行即可
            log.debug("通知设置初始化并发冲突，读取已有行: {}", e.getMessage());
        }
        TbNotifySetting result = notifySettingMapper.selectById(1L);
        return result != null ? result : init;
    }

    @Override
    public void updateSetting(TbNotifySetting setting) {
        setting.setId(1L);
        if (notifySettingMapper.updateById(setting) == 0) {
            notifySettingMapper.insert(setting);
        }
    }

    @Override
    public PageResult<TbNotifyLog> getLogPage(Integer pageNum, Integer pageSize, String type, Integer status) {
        LambdaQueryWrapper<TbNotifyLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq(TbNotifyLog::getType, type);
        }
        if (status != null) {
            wrapper.eq(TbNotifyLog::getStatus, status);
        }
        wrapper.orderByDesc(TbNotifyLog::getCreateTime);
        IPage<TbNotifyLog> result = notifyLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), pageNum, pageSize);
    }

    // ==================== 内部方法 ====================

    /**
     * 发送邮件并写通知日志，失败自动重试 1 次。
     * 必须吞掉所有异常：通知是旁路逻辑，不允许影响主业务。
     */
    private void sendAndLog(String type, String to, String subject, String html) {
        int retries = 1;
        Exception lastError = null;
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(message);
                saveLog(type, subject, to, 1, null, attempt);
                log.info("通知邮件发送成功: type={}, to={}", type, to);
                return;
            } catch (Exception e) {
                lastError = e;
                log.warn("通知邮件发送失败(第{}次): type={}, to={}, {}", attempt + 1, type, to, e.getMessage());
            }
        }
        saveLog(type, subject, to, 0, truncate(lastError != null ? lastError.getMessage() : "unknown"), retries);
    }

    private void saveLog(String type, String title, String recipient, int status, String errorMsg, int retryCount) {
        try {
            TbNotifyLog notifyLog = new TbNotifyLog();
            notifyLog.setType(type);
            notifyLog.setTitle(title);
            notifyLog.setRecipient(recipient);
            notifyLog.setStatus(status);
            notifyLog.setErrorMsg(errorMsg);
            notifyLog.setRetryCount(retryCount);
            notifyLog.setCreateTime(LocalDateTime.now());
            notifyLogMapper.insert(notifyLog);
        } catch (Exception e) {
            log.error("通知日志写入失败: type={}", type, e);
        }
    }

    /**
     * 尝试获取频控名额：Redis SETNX + TTL，拿到才允许发送
     */
    private boolean tryAcquireRateLimit(TbNotifySetting setting, String type) {
        int minutes = setting.getAlertIntervalMinutes() != null && setting.getAlertIntervalMinutes() > 0
                ? setting.getAlertIntervalMinutes() : 30;
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(
                RATE_LIMIT_KEY + type, "1", Duration.ofMinutes(minutes));
        return Boolean.TRUE.equals(ok);
    }

    private boolean isEnabled(Integer flag) {
        return flag != null && flag == 1;
    }

    private String adminRecipient(TbNotifySetting setting) {
        return StrUtil.isNotBlank(setting.getAdminEmail()) ? setting.getAdminEmail() : defaultAdminEmail;
    }

    private String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() > 480 ? msg.substring(0, 480) : msg;
    }
}
