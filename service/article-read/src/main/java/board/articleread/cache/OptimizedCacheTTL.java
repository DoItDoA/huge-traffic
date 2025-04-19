package board.articleread.cache;

import lombok.Getter;

import java.time.Duration;

@Getter
public class OptimizedCacheTTL {
    private Duration logicalTTL;
    private Duration physicalTTL;

    public static final long PHYSICAL_TTL_DELAY_SECONDS = 5;

    public static OptimizedCacheTTL of(long ttlSeconds) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.logicalTTL = Duration.ofSeconds(ttlSeconds); // logical TTL 값 설정
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.logicalTTL.plusSeconds(PHYSICAL_TTL_DELAY_SECONDS); // physical TTL 값 설정, logical + 5
        return optimizedCacheTTL;
    }
}
