package com.example.grzy.service.impl;

import com.example.grzy.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 访问量统计服务实现 — 存储在 Redis
 *
 * @author sodasmell
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VISIT_KEY = "grzy:visit:total";
    private static final String VISIT_TREND_KEY = "grzy:visit:trend:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 趋势 key 过期天数：按天累积的趋势 key 设置 TTL，避免永久滞留 Redis；
     * 看板按天查询最多取近一年，取 370 天兜底
     */
    private static final Duration TREND_KEY_TTL = Duration.ofDays(370);

    @Override
    public void incrementVisit() {
        // 总访问量 +1
        redisTemplate.opsForValue().increment(VISIT_KEY);
        // 每日趋势 +1，并刷新过期时间（防止按天累积的 key 无限增多）
        String dailyKey = VISIT_TREND_KEY + LocalDate.now().format(DATE_FMT);
        redisTemplate.opsForValue().increment(dailyKey);
        redisTemplate.expire(dailyKey, TREND_KEY_TTL);
    }

    @Override
    public long getVisitCount() {
        return parseLong(redisTemplate.opsForValue().get(VISIT_KEY));
    }

    @Override
    public long getDailyVisitCount(String date) {
        String key = VISIT_TREND_KEY + date;
        return parseLong(redisTemplate.opsForValue().get(key));
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
