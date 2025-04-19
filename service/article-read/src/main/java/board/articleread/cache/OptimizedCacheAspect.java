package board.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect // AOP 사용
@Component
@RequiredArgsConstructor
public class OptimizedCacheAspect {
    private final OptimizedCacheManager optimizedCacheManager;

    @Around("@annotation(OptimizedCacheable)") // 해당 어노테이션(@OptimizedCacheable)이 붙은 메서드의 실행 전후를 감싼다
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OptimizedCacheable cacheable = findAnnotation(joinPoint);
        return optimizedCacheManager.process(
                cacheable.type(), // articleViewCount
                cacheable.ttlSeconds(), // 1
                joinPoint.getArgs(), // 해당 어노테이션의 메서드(long count(Long articleId){})의 인자값. 즉, Long articleId
                findReturnType(joinPoint),
                () -> joinPoint.proceed()
        );
    }

    // joinPoint에서 OptimizedCacheable 어노테이션 정보를 찾내 꺼냄
    private OptimizedCacheable findAnnotation(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod().getAnnotation(OptimizedCacheable.class);
    }

    // 반환 타입을 얻어서, 나중에 캐시에서 데이터를 꺼낼 때 역직렬화에 활용
    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }
}
