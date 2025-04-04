package board.common.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    private final String APP_ID = UUID.randomUUID().toString();

    private final int PING_INTERVAL_SECONDS = 3;
    private final int PING_FAILURE_THRESHOLD = 3;

    public AssignedShard assignShards() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1) // 점수 내림차순으로 처음부터 끝까지
                .stream()
                .sorted()
                .toList();
    }

    // 매 PING_INTERVAL_SECONDS 초마다 Redis에 현재 인스턴스가 살아 있다는 것을 ping 보냄
    // 이 모듈은 각자의 모듈(게시글, 좋아요, 댓글 등)에서 돌아가며 3초마다 데이터가 쌓이고 9초 이후에 지워진다.
    // 만약 해당 서비스가 죽으면 redis에 데이터가 없다는 뜻이므로 서비스가 죽었다고 확인 가능. redis 키를 통해 확인 가능하나 모니터링 시스템을 활용
    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();
            // 현재 애플리케이션 인스턴스를 Redis에 기록 (현재 시간과 함께)
            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID); // 현재 시간을 score로 설정

            // 일정 시간 이상 Ping을 보내지 않은 죽은 인스턴스 삭제 (Timeout 기준)
            // minScore와 maxScore 범위 안의 데이터 삭제
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY, // minScore, 가장 오래된 데이터부터
                    Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli() // maxScore, 현재시간의 9초 전까지
                    // Instant.now()는 세계 시간 기준, LocalDateTime.now()는 운영체제 시간 기준
            );
            return null;
        });
    }

    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }

    private String generateKey() {
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }
}
