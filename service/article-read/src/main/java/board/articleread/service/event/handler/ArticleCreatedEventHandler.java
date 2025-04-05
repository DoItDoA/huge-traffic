package board.articleread.service.event.handler;

import board.articleread.repository.ArticleIdListRepository;
import board.articleread.repository.ArticleQueryModel;
import board.articleread.repository.ArticleQueryModelRepository;
import board.articleread.repository.BoardArticleCountRepository;
import board.common.event.Event;
import board.common.event.EventType;
import board.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();

        // 목록이 먼저 생성되고 게시글이 생성되면 순간적으로 목록에서 게시글로 넘어 가는 상황에서 못갈수도 있으니 게시글 생성 먼저 함
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );

        articleIdListRepository.add(payload.getBoardId(), payload.getArticleId(), 1000L); // 최신 게시판과 최신 게시글 아이디를 1000개 저장
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount()); // 게시판에 따른 게시글 개수를 따로 저장
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}
