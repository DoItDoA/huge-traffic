package board.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCalculatorUtils {
    // 자정까지 시간이 얼마나 남았는지 구함
    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT); // .with()는 해당 날짜의 설정된 시간으로 설정
        return Duration.between(now, midnight); // 현재시간과 자정까지의 차이
    }
}
