package board.common.outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {
    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    // applicationEventPublisher.publishEvent() 호출을 읽음, 커밋 되기 전에 실행
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    // applicationEventPublisher.publishEvent() 호출을 읽음, 커밋된 후에 실행
    @Async("messageRelayPublishEventExecutor") // 메서드를 별도의 스레드 풀에서 비동기로 실행, messageRelayPublishEventExecutor 빈으로 등록된 메서드 활용
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    private void publishEvent(Outbox outbox) {
        try {
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS); // 카프카에 토픽 전송 후, 1초 내에 전송이 완료된 반응받지 못하면 예외 발생

            outboxRepository.delete(outbox); // 카프카에 전송이 제대로 되었으면 outbox 내용물은 불필요하므로 삭제
        } catch (Exception e) {
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    @Scheduled(
            fixedDelay = 10, // 이전 작업이 끝난 후, 10초 뒤에 실행
            initialDelay = 5, // 최소 작업 전에 5초 대기 후 실행
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor" // messageRelayPublishPendingEventExecutor 빈으로 등록된 스레드풀을 이용하여 아래의 메서드를 비동기 실행
    ) // Kafka 전송에 실패하여 Outbox에 남아 있는 이벤트를 주기적으로 조회
    public void publishPendingEvent() {
        AssignedShard assignedShard = messageRelayCoordinator.assignShards();
        log.info("[MessageRelay.publishPendingEvent] assignedShard size={}", assignedShard.getShards().size());
        // 샤드별로 10초 이상 지난 이벤트들을 조회하여 Kafka로 다시 전송
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );
            for (Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
    }
}
