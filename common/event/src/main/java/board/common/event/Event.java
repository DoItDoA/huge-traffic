package board.common.event;

import board.common.dataserializer.DataSerializer;
import lombok.Getter;

// 이벤트 객체
@Getter
public class Event<T extends EventPayload> {
    private Long eventId;
    private EventType type;
    private T payload; // 이벤트 객체 내용물

    // 팩토리 메서드
    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    // 받은 이벤트를 직렬화
    public String toJson() {
        return DataSerializer.serialize(this);
    }

    // 이벤트 객체로 역직렬화
    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass()); // 페이로드 타입에 맞춰 저장
        return event;
    }

    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}
