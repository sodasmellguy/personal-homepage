package com.example.grzy.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.example.grzy.common.exception.BusinessException;
import com.example.grzy.common.result.Result;
import com.example.grzy.vo.OnlineSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 在线会话控制器
 * 会话元数据由登录时写入 Redis（grzy:session:info:{token}），此处只读与踢出
 *
 * @author sodasmell
 */
@Slf4j
@Tag(name = "在线会话", description = "在线会话查询与强制下线")
@RestController
@RequestMapping("/api/system/online-session")
@RequiredArgsConstructor
@SaCheckPermission("system:session:view")
public class OnlineSessionController {

    private final StringRedisTemplate stringRedisTemplate;

    /** 会话元数据 Hash Key 前缀（与 AuthServiceImpl 保持一致） */
    private static final String SESSION_INFO_KEY = "grzy:session:info:";

    @Operation(summary = "会话列表", description = "查询当前所有存活会话（自动清理已失效的残留记录）")
    @GetMapping("/list")
    public Result<List<OnlineSessionVO>> list() {
        List<OnlineSessionVO> sessions = new ArrayList<>();
        String currentToken = StpUtil.getTokenValue();
        List<String> deadKeys = new ArrayList<>();

        try (Cursor<String> cursor = stringRedisTemplate.scan(
                ScanOptions.scanOptions().match(SESSION_INFO_KEY + "*").count(200).build())) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String token = key.substring(SESSION_INFO_KEY.length());
                // 会话已在 Sa-Token 侧失效（超时/登出），懒清理残留元数据
                if (StpUtil.getLoginIdByToken(token) == null) {
                    deadKeys.add(key);
                    continue;
                }
                Map<Object, Object> info = stringRedisTemplate.opsForHash().entries(key);
                if (info.isEmpty()) {
                    continue;
                }
                sessions.add(OnlineSessionVO.builder()
                        .token(maskToken(token))
                        .userId(parseLong(info.get("userId")))
                        .username(asStr(info.get("username")))
                        .ip(asStr(info.get("ip")))
                        .userAgent(asStr(info.get("userAgent")))
                        .loginTime(asStr(info.get("loginTime")))
                        .current(token.equals(currentToken))
                        .build());
            }
        }
        if (!deadKeys.isEmpty()) {
            stringRedisTemplate.delete(deadKeys);
        }
        // 最新登录的排在前面（两侧都做 null 兜底，元数据异常时不抛 NPE）
        sessions.sort((a, b) -> StrUtil.nullToEmpty(b.getLoginTime()).compareTo(StrUtil.nullToEmpty(a.getLoginTime())));
        return Result.success(sessions);
    }

    @Operation(summary = "踢下线", description = "强制指定用户下线（单会话策略下等价于踢会话）")
    @DeleteMapping("/{userId}")
    public Result<Void> kick(@PathVariable("userId") Long userId) {
        if (userId != null && userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能踢出自己的当前会话，请使用退出登录");
        }
        StpUtil.kickout(userId);
        // 残留的会话元数据由列表接口懒清理（TTL 兜底）
        log.info("会话被强制下线: operator={}, targetUserId={}", StpUtil.getLoginIdAsLong(), userId);
        return Result.success();
    }

    private String asStr(Object value) {
        return value != null ? value.toString() : "";
    }

    private Long parseLong(Object value) {
        try {
            return value != null ? Long.parseLong(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String maskToken(String token) {
        return token.length() > 8 ? token.substring(0, 8) + "..." : token;
    }
}
