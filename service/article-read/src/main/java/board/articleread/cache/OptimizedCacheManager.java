package board.articleread.cache;

import board.common.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    public Object process(String type, long ttlSeconds, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> originDataSupplier) throws Throwable {
        String key = generateKey(type, args); // articleViewCount::{articleId}

        String cachedData = redisTemplate.opsForValue().get(key);
        // redis에 캐시가 없을시 바로 원본 호출
        if (cachedData == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 캐시가 존재할 시 역직렬화
        OptimizedCache optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);
        // 캐시에 값이 있지만 유효하지 않거나 역직렬화 실패시
        if (optimizedCache == null) {
            return refresh(originDataSupplier, key, ttlSeconds);
        }

        // 만약 현재 시간이 현재시간+LogicalTTL보다 이전일 경우
        if (!optimizedCache.isExpired()) {
            // 캐시데이터 호출
            return optimizedCache.parseData(returnType); // 리턴 타입 long으로 역직렬화
        }

        // LogicalTTL은 만료되었고 PhysicalTTL은 살아있는 시점
        // 락이 있으면 캐시 호출, 없으면 락 생성하고 패스
        if (!optimizedCacheLockProvider.lock(key)) {
            // 첫 요청이 락을 생성하여 원본 만드는 동안 다른 요청이 오면 캐시데이터 전달
            return optimizedCache.parseData(returnType);
        }

        try {
            return refresh(originDataSupplier, key, ttlSeconds);
        } finally {
            optimizedCacheLockProvider.unlock(key);
        }
    }

    // 원본에 요청하여 값을 가져오고 physical, logical TTL을 새로 구함
    private Object refresh(OptimizedCacheOriginDataSupplier<?> originDataSupplier, String key, long ttlSeconds) throws Throwable {
        Object result = originDataSupplier.get(); // 내부에서 joinPoint.proceed() 호출됨, view count 개수 호출

        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSeconds); // physical TTL과 logical TTL 구하기
        OptimizedCache optimizedCache = OptimizedCache.of(result, optimizedCacheTTL.getLogicalTTL());

        redisTemplate.opsForValue()
                .set(key,
                        DataSerializer.serialize(optimizedCache),
                        optimizedCacheTTL.getPhysicalTTL()
                ); // 카운트 개수, 현재 시간+logicalTTL, physicalTTL(ex 6초) 저장

        return result;
    }

    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(joining(DELIMITER));
    }

}
