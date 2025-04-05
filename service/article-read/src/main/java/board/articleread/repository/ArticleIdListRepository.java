package board.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{boardId}::article-list
    private static final String KEY_FORMAT = "article-read::board::%s::article-list";

    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey(boardId);
            conn.zAdd(key, 0, toPaddedString(articleId)); // long을 double로 만들면 일부 유실되어 스코어 값을 동일하게 하고 value 값을 통해 오름차순
            conn.zRemRange(key, 0, -limit - 1);
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddedString(articleId));
    }

    // 페이지네이션
    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return redisTemplate.opsForZSet()
                .reverseRange(generateKey(boardId), offset, offset + limit - 1) // score 내림차순
                .stream().map(Long::valueOf).toList();
    }

    // 무한 스크롤
    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        return redisTemplate.opsForZSet().reverseRangeByLex( // value값 기준으로 내림차순
                generateKey(boardId),
                lastArticleId == null ? // 범위 구하기
                        Range.unbounded() : // 전부다 가져오기
                        Range.leftUnbounded( // 괄호 안의 값 기준으로 작은 것을 가져옴
                                Range.Bound.exclusive(toPaddedString(lastArticleId)) // exclusive를 통해 해당값은 포함안하고 작은값 가져오기
                        ),
                Limit.limit().count(limit.intValue()) // 출력 개수 제한
        ).stream().map(Long::valueOf).toList();
    }

    private String toPaddedString(Long articleId) {
        return "%019d".formatted(articleId);
        // 1234 -> 0000000000000001234
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}
