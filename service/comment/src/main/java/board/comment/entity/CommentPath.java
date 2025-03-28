package board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable // Embedded 되기 위해 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path; // 컬럼명 Path, @Column 붙여서도 사용 가능

    // static 또는 final 이 붙으면 JPA 가 DB 컬럼 연관에 무시함
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5; // depth가 최대 5개

    // MIN_CHUNK = "00000", MAX_CHUNK = "zzzzz"
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path) {
        if (isDepthOverflowed(path)) { // 경로 길이가 설정한 최대 25자리보다 크면 에러
            throw new IllegalStateException("depth overflowed");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflowed(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if (descendantsTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK); // 기존 path에 새것(00000) 붙이기
        }
        String childrenTopPath = findChildrenTopPath(descendantsTopPath); // descendantsTopPath에서 본인 depth 영역까지 구하기
        String increase = increase(childrenTopPath);
        return CommentPath.create(increase);
    }

    private String findChildrenTopPath(String descendantsTopPath) {
        int depth = calDepth(this.path); // 부모가 몇 Depth 인지 구하기
        return descendantsTopPath.substring(0, (depth + 1) * DEPTH_CHUNK_SIZE); // 부모 depth+1하고 * 5하여 추출, 나의 depth 중 가장 큰 값 구함
    }

    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverflowed(lastChunk)) {
            throw new IllegalStateException("chunk overflowed");
        }

        int charsetLength = CHARSET.length();

        // 62진법을 10진법으로 변환
        // abcde -> a*62^4 + b*62^3 + c*62^2 + d*62^1 + e*62^0 -> ((((62*0+a)*62+b)*62+c)*62+d)*62+e
        int value = 0;
        for (char ch : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        value = value + 1; // 10진법 변환후 +1

        // 10진법을 62진법으로 다시 변환
        String result = "";
        for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            int val = value % charsetLength;
            result = CHARSET.charAt(val) + result;
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverflowed(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }

}
