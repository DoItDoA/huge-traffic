package board.articleread.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching // @Cacheable 사용 가능
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) { // RedisConnectionFactory 레디스와 연결하는 객체
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(
                        Map.of("articleViewCount", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1))) // @Cacheable로 articleViewCount는 1초 동안만 캐시로 사용 설정
                )
                .build();
    }
}
