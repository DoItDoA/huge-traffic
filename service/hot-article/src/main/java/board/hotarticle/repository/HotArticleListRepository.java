package board.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        // 여러 개의 명령어를 한 번에 실행하여 성능 향상(배치 처리)
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(time);
            conn.zAdd(key, score, String.valueOf(articleId));
            conn.zRemRange(key, 0, -limit - 1); // 상위 0~ limit 개 유지, 벗어나면 나머지는 삭제
            conn.expire(key, ttl.toSeconds());
            return null;
        });
    }

    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String dateStr) {
        return KEY_FORMAT.formatted(dateStr);
    }

    public List<Long> readAll(String dateStr) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(generateKey(dateStr), 0, -1) // 내림차순 정렬된 데이터 조회 , Set<TypedTuple<V>> 반환(밸류와 스코어 보유)
                .stream()
                .peek(tuple ->
                        log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore())) // 각 데이터 로그 출력, peek는 중간 변형 없이 로그 출력등 활용
                .map(ZSetOperations.TypedTuple::getValue) // 게시글 id 만 추출, ZSetOperations.TypedTuple은 value와 score를 가지고 있는 객체
                .map(Long::valueOf)
                .toList();
    }
}
