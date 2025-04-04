package board.common.outboxmessagerelay;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync // @Async 붙은 메서드를 별도의 스레드에서 실행하도록 함, 비동기 실행
@Configuration
@ComponentScan("board.common.outboxmessagerelay")
@EnableScheduling // 전송되지 않은 이벤트를 주기적으로 검토
public class MessageRelayConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, String> messageRelayKafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
    }

    @Bean // 트랜잭션이 끝날때마다 이벤트 전송을 비동기로 전송하기위해 처리하는 스레드풀
    public Executor messageRelayPublishEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // 기본적으로 실행할 스레드풀을 20개 생성
        executor.setMaxPoolSize(50); // 최대 50개까지 늘리게 함
        executor.setQueueCapacity(100); // 작업이 몰릴 경우 대기할 수 있는 큐 크기를 100개로 함, 스레드가 꽉차면 작업들이 큐에서 대기탐
        executor.setThreadNamePrefix("mr-pub-event-"); // 생성된 스레드 이름 prefix 설정
        return executor;
    }

    @Bean
    public Executor messageRelayPublishPendingEventExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
