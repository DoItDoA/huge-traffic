package board.view;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "board") // outbox는 다른 패키지이므로 @SpringBootApplication이 읽을 수가 없어 따로 추가
@SpringBootApplication // 기본적으로 자신이 포함된 하위 패키지를 전부 스캔하지만, 다른 패키지일 경우 Entity와 Repository는 스캔하지 않는다
@EnableJpaRepositories(basePackages = "board") // outbox는 다른 패키지이므로 @SpringBootApplication이 읽을 수가 없어 따로 추가
public class ViewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewApplication.class, args);
    }
}
