package board.view.service;

//import board.common.event.EventType;
//import board.common.event.payload.ArticleViewedEventPayload;
//import board.common.outboxmessagerelay.OutboxEventPublisher;
import board.common.event.EventType;
import board.common.event.payload.ArticleViewedEventPayload;
import board.common.outboxmessagerelay.OutboxEventPublisher;
import board.view.entity.ArticleViewCount;
import board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if (result == 0) {
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> { },
                        () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount))
                    );
            // optional.ifPresentOrElse(
            //    value -> { /* 값이 존재할 때 실행할 코드 */ },
            //    () -> { /* 값이 없을 때 실행할 코드 */ }
            //);
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}
