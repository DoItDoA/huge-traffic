package board.hotarticle.service.eventhandler;

import board.common.event.Event;
import board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event); // 이벤트 받았을 때 처리
    boolean supports(Event<T> event); // 구현체가 지원되는지 확인
    Long findArticleId(Event<T> event); // 이벤트가 어떤 게시글 id를 찾아주는지 확인
}
