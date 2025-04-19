package board.articleread.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import board.common.dataserializer.DataSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
public class OptimizedCache {
    private String data;
    private LocalDateTime expiredAt;

    public static OptimizedCache of(Object data, Duration ttl) {
        OptimizedCache optimizedCache = new OptimizedCache();
        optimizedCache.data = DataSerializer.serialize(data); // view 카운트 개수
        optimizedCache.expiredAt = LocalDateTime.now().plus(ttl); // 현재시간에 logical TTL 더함
        return optimizedCache;
    }

    // get..() 혹은 is..() 형태의 public 메서드는 속성처럼 간주돼서 JSON에 포함될 수 있다. {expired:true} 이렇게 포함된다.
   @JsonIgnore // Redis나 JSON으로 저장할 때 포함되지 않도록 숨기는 역할, 즉 isExpired()라는 필드나 값이 JSON으로 저장되지 않음
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt); // 현재시간이 expiredAt 보다 이후인지 확인
    }

    public <T> T parseData(Class<T> dataType) {
        return DataSerializer.deserialize(data, dataType);
    }
}
