package com.example.grzy.task;

import com.example.grzy.service.impl.ArticleTxSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 文章定时发布任务
 * 每分钟扫描一次"定时发布"状态的文章，到达发布时间后自动改为"已发布"
 * <p>
 * 事务体已抽至 {@link ArticleTxSupport#publishDueArticles()}（独立 Bean 调用，保证 @Transactional 生效），
 * 本类只保留调度入口、防重入锁与日志。
 *
 * @author sodasmell
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticlePublishTask {

    /** 定时发布任务防重入锁 key */
    private static final String LOCK_KEY = "grzy:task:lock:publish";
    /** 锁 TTL：任务间隔 60 秒的 2 倍，仅在宕机等未正常释放时兜底 */
    private static final Duration LOCK_TTL = Duration.ofSeconds(120);

    /** 事务支撑 Bean：跨 Bean 调用事务方法，避免 this 自调用绕过 AOP 代理 */
    private final ArticleTxSupport articleTxSupport;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void publishScheduledArticles() {
        if (!tryLock()) {
            return;
        }
        try {
            articleTxSupport.publishDueArticles();
        } finally {
            unlock();
        }
    }

    /**
     * 应用启动时立即执行一次（覆盖停机期间错过的发布）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void publishOnStartup() {
        if (!tryLock()) {
            return;
        }
        try {
            articleTxSupport.publishDueArticles();
        } finally {
            unlock();
        }
    }

    /**
     * 防重入加锁：拿到锁才执行，拿不到说明已有实例在执行，直接跳过本次
     */
    private boolean tryLock() {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "1", LOCK_TTL);
        if (!Boolean.TRUE.equals(ok)) {
            log.info("定时发布任务已有实例在执行，本次跳过: key={}", LOCK_KEY);
            return false;
        }
        return true;
    }

    /**
     * 执行完主动释放锁，避免残留锁拦截下一次调度（TTL 仅作异常兜底）
     */
    private void unlock() {
        try {
            stringRedisTemplate.delete(LOCK_KEY);
        } catch (Exception e) {
            log.warn("释放定时发布任务锁失败: key={}", LOCK_KEY, e);
        }
    }
}
