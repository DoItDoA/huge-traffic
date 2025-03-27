package board.articleread.service.event.handler;

import board.common.event.Event;
import board.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
