package com.portfolio.backend.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RetryUtils {

    /**
     * 낙관적 락 예외가 발생할 경우 지정된 횟수만큼 재시도하는 메서드
     * 
     * @param operation 재시도할 작업
     * @param maxRetry 최대 재시도 횟수
     * @param retryLogPrefix 로그에 표시할 접두사
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetry, String retryLogPrefix) {
        int attempts = 0;
        while (attempts < maxRetry) {
            try {
                return operation.execute();
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= maxRetry) {
                    log.error("{} - 최대 재시도 횟수를 초과했습니다: {}", retryLogPrefix, e.getMessage());
                    throw e;
                }
                log.warn("{} - 낙관적 락 충돌 발생, 재시도 {}/{}", retryLogPrefix, attempts, maxRetry);
                try {
                    // 지수 백오프 방식으로 대기 시간 증가
                    Thread.sleep(50 * (1 << attempts));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                }
            }
        }
        // 이 코드는 실행되지 않아야 함
        throw new RuntimeException("예상치 못한 오류");
    }

    /**
     * 낙관적 락 예외가 발생할 경우 기본 3회 재시도하는 메서드
     *
     * @param operation 재시도할 작업
     * @param retryLogPrefix 로그에 표시할 접두사
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, String retryLogPrefix) {
        return executeWithRetry(operation, 3, retryLogPrefix);
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute();
    }
}
