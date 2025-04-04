package board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Getter // 샤드를 애플리케이션에 균등하게 분배
public class AssignedShard {
    private List<Long> shards; // 애플리케이션에 할당된 샤드번호

    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds);
        if (appIndex == -1) {
            return List.of();
        }
        // 범위 구하기
        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() - 1;
        /* 만약 샤드가 100개이고 앱이 4개이면, appIndex는 0~3까지
           appIndex = 0 -> start=0 ,end=24
           appIndex = 1 -> start=25 ,end=49
           appIndex = 2 -> start=50 ,end=74
           appIndex = 3 -> start=75 ,end=99
           즉 샤드를 앱에 적절히 분배 시킴
        */

        Stream<Long> boxed = LongStream.rangeClosed(start, end).boxed();
        return LongStream.rangeClosed(start, end) // LongStream 타입 start부터 end까지 값 보유, 예시) 2(start) ~ 6(end) 값이 stream에 들어 있음
                .boxed() // Stream<Long> 타입, LongStream은 long 스트림이지만 객체형 Long으로 변환
                .toList();
    }

    // 애플리케이션 아이디가 몇번째에 있는지 반환
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i = 0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}
