package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.event.UserCreditAmountChangedEvent;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse;
import com.portfolio.backend.service.user.fixture.UserCreditServiceRequestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@DisplayName("UserCreditService 테스트")
class UserCreditServiceTest extends ServiceTest {

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Autowired
    private UserCreditService userCreditService;

    @MockBean
    protected DomainEventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        userCreditRepository.deleteAll();
    }

    @Nested
    @DisplayName("크레딧 조회 시")
    class GetCurrentCreditTest {

        @Test
        @DisplayName("사용자의 크레딧을 조회할 수 있다")
        void shouldAllowAuthenticatedUserToGetCurrentCredit() {
            // Given
            UserCredit credit = new UserCredit(user);
            credit.add(new Money(BigDecimal.valueOf(10000)));
            userCreditRepository.save(credit);

            Long userId = user.getId();

            // When
            UserCreditServiceResponse.Get response = userCreditService.getCurrentCredit(userId);

            // Then
            assertThat(response.amount()).isEqualTo(credit.getAmount());
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // Given
            Long userId = adminUser.getId();

            // When & Then
            assertThatThrownBy(() -> userCreditService.getCurrentCredit(userId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("크레딧 증가 시")
    class IncreaseTest {

        @Test
        @DisplayName("사용자의 크레딧을 추가할 수 있다")
        void shouldAllowAuthenticatedUserToAddCredit() {
            // Given
            userCreditRepository.save(new UserCredit(user));

            Long userId = user.getId();
            BigDecimal amount = BigDecimal.valueOf(10000);
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(amount);

            // When
            userCreditService.increase(userId, request);

            // Then
            UserCredit response = userCreditRepository.findByUserId(userId).get();
            assertThat(response.getAmount()).isEqualTo(new Money(amount));
        }

        @Test
        @DisplayName("크레딧 충전시 이벤트가 발생한다")
        void shouldPublishUserCreditAmountChangedEventWhenCreditIncreased() {
            // Given
            userCreditRepository.save(new UserCredit(user));

            Long userId = user.getId();
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(BigDecimal.valueOf(10000));

            // When
            userCreditService.increase(userId, request);

            // Then
            ArgumentCaptor<UserCredit> userCreditCaptor = ArgumentCaptor.forClass(UserCredit.class);
            verify(eventPublisher).publishEventsFrom(userCreditCaptor.capture());
            UserCredit capturedUserCredit = userCreditCaptor.getValue();

            boolean hasExpectedEvent = capturedUserCredit.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof UserCreditAmountChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 UserCreditAmountChangedEvent가 포함되어 있어야 합니다");
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // Given
            Long userId = adminUser.getId();
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(BigDecimal.valueOf(10000));

            // When & Then
            assertThatThrownBy(() -> userCreditService.increase(userId, request))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("크레딧 감소 시")
    class DecreaseTest {

        @Test
        @DisplayName("사용자의 크레딧을 차감할 수 있다")
        void shouldDecreaseUserCredit() {
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money money = new Money(BigDecimal.valueOf(11000));
            userCredit.add(money);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            Money deductionMoney = new Money(BigDecimal.valueOf(10000));

            // When
            userCreditService.pay(userId, deductionMoney);

            // Then
            UserCredit response = userCreditRepository.findByUserId(userId).get();
            assertThat(response.getAmount()).isEqualTo(money.subtract(deductionMoney));
        }

        @Test
        @DisplayName("크레딧 차감시 이벤트가 발생한다")
        void shouldPublishUserCreditAmountChangedEventWhenCreditDecreased() {
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money money = new Money(BigDecimal.valueOf(11000));
            userCredit.add(money);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            Money deductionMoney = new Money(BigDecimal.valueOf(10000));

            // When
            userCreditService.pay(userId, deductionMoney);

            // Then
            ArgumentCaptor<UserCredit> userCreditCaptor = ArgumentCaptor.forClass(UserCredit.class);
            verify(eventPublisher).publishEventsFrom(userCreditCaptor.capture());
            UserCredit capturedUserCredit = userCreditCaptor.getValue();

            boolean hasExpectedEvent = capturedUserCredit.getDomainEvents().stream()
                    .anyMatch(event -> event instanceof UserCreditAmountChangedEvent);

            assertTrue(hasExpectedEvent, "도메인 이벤트에 UserCreditAmountChangedEvent가 포함되어 있어야 합니다");
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // Given
            Long userId = user.getId();
            Money amount = new Money(BigDecimal.valueOf(10000));

            // When, Then
            assertThatThrownBy(() -> userCreditService.pay(userId, amount))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("동시성 처리 시")
    class ConcurrencyTest {

        @Test
        @DisplayName("UserCredit 증가와 감소 연산이 동시에 발생해도 일관성이 유지된다")
        void shouldMaintainConsistencyWithSimultaneousOperations() throws Exception{
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(10000));
            userCredit.add(initialAmount);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            Money increaseAmount = new Money(BigDecimal.valueOf(5000));
            Money decreaseAmount = new Money(BigDecimal.valueOf(3000));
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(increaseAmount.getAmount());

            // When - 동시에 실행
            CountDownLatch latch = new CountDownLatch(2);
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            Future<?> increaseFuture = executorService.submit(() -> {
                try {
                    userCreditService.increase(userId, request);
                } finally {
                    latch.countDown();
                }
            });

            Future<?> decreaseFuture = executorService.submit(() -> {
                try {
                    userCreditService.pay(userId, decreaseAmount);
                } finally {
                    latch.countDown();
                }
            });

            latch.await(10, TimeUnit.SECONDS); // 최대 10초 대기
            increaseFuture.get(); // 예외 확인
            decreaseFuture.get(); // 예외 확인
            executorService.shutdown();

            // Then
            UserCredit updatedCredit = userCreditRepository.findByUserId(userId).get();
            Money expectedAmount = initialAmount.add(increaseAmount).subtract(decreaseAmount);
            assertThat(updatedCredit.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("재시도 로직을 통해 낙관적 락 예외를 처리하여 모든 요청을 성공시킬 수 있다")
        void shouldHandleOptimisticLockingWithRetry() {
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(10000));
            userCredit.add(initialAmount);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            int retryCount = 3;
            Money increaseAmount = new Money(BigDecimal.valueOf(1000));
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(increaseAmount.getAmount());

            // When
            for (int i = 0; i < retryCount; i++) {
                try {
                    userCreditService.increase(userId, request);
                } catch (Exception e) {
                    // 실패시 재시도
                    if (i == retryCount - 1) {
                        throw e;
                    }
                    continue;
                }
                break;
            }

            // Then
            UserCredit updatedCredit = userCreditRepository.findByUserId(userId).get();
            Money expectedAmount = initialAmount.add(increaseAmount);
            assertThat(updatedCredit.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("비관적 락으로 인해 동시 요청 시 순차적으로 처리된다")
        void shouldProcessSimultaneousRequestsSequentially() throws Exception{
            // Given
            UserCredit userCredit = new UserCredit(user);
            userCredit.add(new Money(BigDecimal.valueOf(10000)));
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            int numberOfThreads = 10;
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestFixtures.createIncrease(BigDecimal.valueOf(1000));
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

            // When
            Runnable increaseTask = () -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록
                    userCreditService.increase(userId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Exception in thread: " + Thread.currentThread().getName() +
                            ", Message: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            };

            Thread[] threads = new Thread[numberOfThreads];
            for (int i = 0; i < numberOfThreads; i++) {
                threads[i] = new Thread(increaseTask);
                threads[i].start();
            }

            startLatch.countDown(); // 모든 스레드 시작
            endLatch.await(10, TimeUnit.SECONDS); // 모든 스레드 완료 대기

            // Then
            UserCredit finalCredit = userCreditRepository.findByUserId(userId).get();
            assertThat(successCount.get()).isEqualTo(numberOfThreads); // 모든 요청이 성공
            assertThat(finalCredit.getAmount())
                    .isEqualTo(new Money(BigDecimal.valueOf(20000))); // 10000 + (1000 * 10)
        }


        @Test
        @DisplayName("비관적 락을 통해 동시 접근을 차단할 수 있다")
        void shouldBlockSimultaneousAccessWithPessimisticLock() throws InterruptedException {
            // Given
            UserCredit userCredit = new UserCredit(user);
            userCredit.add(new Money(BigDecimal.valueOf(10000)));
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(2);
            AtomicBoolean secondThreadStarted = new AtomicBoolean(false);
            AtomicReference<Exception> threadException = new AtomicReference<>();

            // When
            Thread thread1 = new Thread(() -> {
                try {
                    startLatch.await();
                    userCreditService.pay(userId, new Money(BigDecimal.valueOf(1000)));
                    Thread.sleep(1000); // 첫 번째 스레드가 락을 보유하는 시간
                } catch (Exception e) {
                    threadException.set(e);
                } finally {
                    endLatch.countDown();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    startLatch.await();
                    secondThreadStarted.set(true);
                    userCreditService.pay(userId, new Money(BigDecimal.valueOf(1000)));
                } catch (Exception e) {
                    threadException.set(e);
                } finally {
                    endLatch.countDown();
                }
            });

            thread1.start();
            thread2.start();
            startLatch.countDown();

            // Then
            endLatch.await(5, TimeUnit.SECONDS);
            assertThat(secondThreadStarted.get()).isTrue(); // 두 번째 스레드가 시작되었는지 확인
            assertThat(threadException.get()).isNull(); // 예외가 발생하지 않았는지 확인

            UserCredit finalCredit = userCreditRepository.findByUserId(userId).get();
            assertThat(finalCredit.getAmount())
                    .isEqualTo(new Money(BigDecimal.valueOf(8000))); // 10000 - 1000 - 1000
        }

    }
}
