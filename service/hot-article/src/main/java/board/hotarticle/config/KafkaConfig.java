package board.hotarticle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;


@Configuration
public class KafkaConfig {
    // ConsumerFactory<String, String> consumerFactory
    // kafka 컨슈머 객체 생성 팩토리
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>(); // Kafka 리스너를 동작시키기 위한 컨테이너 팩토리, <키 타입, 밸류 타입>
        // ConcurrentKafkaListenerContainerFactory Concurrent가 붙어서 비동기(멀티스레드)로 Kafka를 돌리며 스레드의 개수는 partition의 개수에 맞추어 늘어남
        // @KafkaListener를 사용할 때 어떤 설정을 적용할 지 지정
        factory.setConsumerFactory(consumerFactory); // kafka 리스너가 컨슈머 생성할 때 사용하도록 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 개발자가 acknowledge() 호출하여 직접 커밋, yml에서 enable-auto-commit: false 설정 해야함
        return factory;
    }
}
