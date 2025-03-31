package board.common.event;

import board.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.BOARD_ARTICLE), // 게시글 생성
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.BOARD_ARTICLE), // 게시글 수정
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.BOARD_ARTICLE), // 게시글 삭제
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.BOARD_COMMENT), // 댓글 생성
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.BOARD_COMMENT), // 댓글 삭제
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.BOARD_LIKE), // 좋아요
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.BOARD_LIKE), // 좋아요 취소
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.BOARD_VIEW) // 조회수
    ;

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type); // type = ARTICLE_CREATED 이면 거기에 맞는 클래스와 토픽 반환
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String BOARD_ARTICLE = "board-article";
        public static final String BOARD_COMMENT = "board-comment";
        public static final String BOARD_LIKE = "board-like";
        public static final String BOARD_VIEW = "board-view";
    }
}
