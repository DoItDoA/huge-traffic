package board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OptimizedCacheLockProvider {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "optimized-cache-lock::";
    private static final Duration LOCK_TTL = Duration.ofSeconds(3); // 3초 뒤 락이 풀리게 설정

    // 저장 성공 시 true
    public boolean lock(String key) {
        return redisTemplate.opsForValue().setIfAbsent(
                generateLockKey(key),
                "",
                LOCK_TTL
        );
    }

    public void unlock(String key) {
        redisTemplate.delete(generateLockKey(key));
    }

    private String generateLockKey(String key) {
        return KEY_PREFIX + key;
    }
}
