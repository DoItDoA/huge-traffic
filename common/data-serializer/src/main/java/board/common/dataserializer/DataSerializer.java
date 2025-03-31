package board.common.dataserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
* json 기반의 직렬화 역직렬화 변경
* * */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSerializer { // final 붙이는 이유는 상속불가하게 하여 오버라이딩으로 내부 코드가 바뀌지 않게 함

    private static final ObjectMapper objectMapper = initialize(); // objectMapper는 직렬화, 역직렬화를 담당하는 주요 클래스

    private static ObjectMapper initialize() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule()) // 직렬화시 LocalDate, LocalDateTime 변경 가능 적용
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // JSON 안에 있는 필드가 Java 클래스에는 존재하지 않아도 에러를 발생시키지 말고 그냥 무시해라
    }
    // JSON을 역직렬화
    public static <T> T deserialize(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.deserialize] data={}, clazz={}", data, clazz, e);
            return null;
        }
    }

    // 모든 타입을 역직렬화
    public static <T> T deserialize(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    // 객체를 직렬화
    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object); // 직렬화
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.serialize] object={}", object, e);
            return null;
        }
    }
}
