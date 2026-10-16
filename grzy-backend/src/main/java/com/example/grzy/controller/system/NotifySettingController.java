package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.TbNotifyLog;
import com.example.grzy.entity.TbNotifySetting;
import com.example.grzy.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件通知设置控制器
 *
 * @author sodasmell
 */
@Tag(name = "通知设置", description = "邮件通知开关配置与通知日志查询")
@RestController
@RequestMapping("/api/system/notify")
@RequiredArgsConstructor
@SaCheckPermission("system:notify:view")
public class NotifySettingController {

    private final NotifyService notifyService;

    @Operation(summary = "获取通知设置", description = "获取邮件通知配置（不存在则返回默认值）")
    @GetMapping("/setting")
    public Result<TbNotifySetting> getSetting() {
        return Result.success(notifyService.getSetting());
    }

    @Operation(summary = "更新通知设置", description = "更新邮件通知开关、站长收件邮箱与告警频控间隔")
    @PutMapping("/setting")
    public Result<Void> updateSetting(@RequestBody TbNotifySetting setting) {
        notifyService.updateSetting(setting);
        return Result.success();
    }

    @Operation(summary = "通知日志列表", description = "分页查询邮件通知发送日志")
    @GetMapping("/log/list")
    public Result<PageResult<TbNotifyLog>> logList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(notifyService.getLogPage(pageNum, pageSize, type, status));
    }
}
