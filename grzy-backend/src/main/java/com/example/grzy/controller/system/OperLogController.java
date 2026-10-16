package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.example.grzy.common.result.PageResult;
import com.example.grzy.common.result.Result;
import com.example.grzy.entity.SysOperLog;
import com.example.grzy.service.OperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 *
 * @author sodasmell
 */
@Tag(name = "操作日志", description = "操作日志管理接口")
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
@SaCheckPermission("system:log:view")
public class OperLogController {

    private final OperLogService operLogService;

    @Operation(summary = "操作日志列表", description = "分页查询操作日志")
    @GetMapping("/list")
    public Result<PageResult<SysOperLog>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "username", required = false) String username) {
        PageResult<SysOperLog> pageResult = operLogService.getLogPage(pageNum, pageSize, module, username);
        return Result.success(pageResult);
    }
}
