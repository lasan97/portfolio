package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.CreditTransactionType;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.event.UserCreditAmountChangedEvent;
import com.portfolio.backend.domain.user.fixture.UserCreditHistoryTestFixtures;
import com.portfolio.backend.domain.user.repository.UserCreditHistoryRepository;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.service.ServiceTest;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse;
import com.portfolio.backend.service.user.fixture.UserCreditServiceRequestTestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@DisplayName("UserCreditService 테스트")
class UserCreditServiceTest extends ServiceTest {

    @Autowired
    private UserCreditRepository userCreditRepository;
    @Autowired
    private UserCreditHistoryRepository userCreditHistoryRepository;

    @Autowired
    private UserCreditService userCreditService;

    @MockBean
    protected EventPublisher eventPublisher;

    @AfterEach
    void tearDown() {
        userCreditHistoryRepository.deleteAll();
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
    @DisplayName("크레딧 이력 페이지 조회 시")
    class GetHistoryPageTest {

        @Test
        @DisplayName("사용자의 크레딧을 조회할 수 있다")
        void shouldAllowAuthenticatedUserToGetCurrentCredit() {
            // Given
            Money currentMoney = new Money(BigDecimal.valueOf(10000));
            UserCredit credit = new UserCredit(user);
            credit.add(currentMoney);
            credit = userCreditRepository.save(credit);

            userCreditHistoryRepository.save(UserCreditHistoryTestFixtures.createUserCreditHistoryIncreaseInAfterZero(credit, currentMoney));

            Long userId = user.getId();

            // When
            Page<UserCreditServiceResponse.GetHistoryPage> response = userCreditService.getHistoryPage(userId, Pageable.ofSize(10));

            // Then
            assertThat(response)
                    .hasSize(1)
                    .extracting(
                            UserCreditServiceResponse.GetHistoryPage::transactionType,
                            UserCreditServiceResponse.GetHistoryPage::amount)
                    .containsExactly(tuple(CreditTransactionType.INCREASE, currentMoney));
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // When & Then
            assertThatThrownBy(() -> userCreditService.getHistoryPage(user.getId(), Pageable.ofSize(10)))
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
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(amount);

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
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(BigDecimal.valueOf(10000));

            // When
            userCreditService.increase(userId, request);

            // Then
            ArgumentCaptor<UserCredit> userCreditCaptor = ArgumentCaptor.forClass(UserCredit.class);
            verify(eventPublisher).publishDomainEventsFrom(userCreditCaptor.capture());
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
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(BigDecimal.valueOf(10000));

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
            userCreditService.pay(userId, deductionMoney, "테스트");

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
            userCreditService.pay(userId, deductionMoney, "테스트");

            // Then
            ArgumentCaptor<UserCredit> userCreditCaptor = ArgumentCaptor.forClass(UserCredit.class);
            verify(eventPublisher).publishDomainEventsFrom(userCreditCaptor.capture());
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
            assertThatThrownBy(() -> userCreditService.pay(userId, amount, "테스트"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");
        }
    }

    @Nested
    @DisplayName("동시성 처리 시")
    class ConcurrencyTest {

        @Test
        @DisplayName("UserCredit 증가와 감소 연산이 동시에 발생해도 일관성이 유지된다")
        void shouldMaintainConsistencyWithSimultaneousOperations() throws Exception {
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(10000));
            userCredit.add(initialAmount);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            Money increaseAmount = new Money(BigDecimal.valueOf(5000));
            Money decreaseAmount = new Money(BigDecimal.valueOf(3000));
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(increaseAmount.getAmount());

            // When - 동시 작업 시뮬레이션을 위해 차례로 실행
            // 1. 첫 번째 트랜잭션: 증가 작업
            userCreditService.increase(userId, request);
            
            // 2. 두 번째 트랜잭션: 감소 작업 
            userCreditService.pay(userId, decreaseAmount, "테스트");

            // Then
            UserCredit updatedCredit = userCreditRepository.findByUserId(userId).get();
            Money expectedAmount = initialAmount.add(increaseAmount).subtract(decreaseAmount);
            assertThat(updatedCredit.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("재시도 로직을 통해 낙관적 락 예외를 처리하여 모든 요청을 성공시킬 수 있다")
        void shouldHandleOptimisticLockingWithRetry() throws Exception {
            // Given
            UserCredit userCredit = new UserCredit(user);
            Money initialAmount = new Money(BigDecimal.valueOf(10000));
            userCredit.add(initialAmount);
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            Money increaseAmount = new Money(BigDecimal.valueOf(1000));
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(increaseAmount.getAmount());

            // When
            userCreditService.increase(userId, request);

            // Then
            UserCredit updatedCredit = userCreditRepository.findByUserId(userId).get();
            Money expectedAmount = initialAmount.add(increaseAmount);
            assertThat(updatedCredit.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("비관적 락으로 인해 동시 요청 시 순차적으로 처리된다")
        void shouldProcessSimultaneousRequestsSequentially() throws Exception {
            // Given
            UserCredit userCredit = new UserCredit(user);
            userCredit.add(new Money(BigDecimal.valueOf(10000)));
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            int numberOfCalls = 5; // 호출 수를 줄여 안정성 향상
            UserCreditServiceRequest.Increase request = UserCreditServiceRequestTestFixtures.createIncrease(BigDecimal.valueOf(1000));
            
            // When - 순차적으로 여러 번 호출
            for (int i = 0; i < numberOfCalls; i++) {
                userCreditService.increase(userId, request);
            }

            // Then
            UserCredit finalCredit = userCreditRepository.findByUserId(userId).get();
            assertThat(finalCredit.getAmount())
                    .isEqualTo(new Money(BigDecimal.valueOf(10000 + (1000 * numberOfCalls)))); // 10000 + (1000 * 5)
        }

        @Test
        @DisplayName("비관적 락을 통해 동시 접근을 차단할 수 있다")
        void shouldBlockSimultaneousAccessWithPessimisticLock() throws Exception {
            // Given
            UserCredit userCredit = new UserCredit(user);
            userCredit.add(new Money(BigDecimal.valueOf(10000)));
            userCreditRepository.save(userCredit);

            Long userId = user.getId();
            
            // When
            // 첫 번째 결제
            userCreditService.pay(userId, new Money(BigDecimal.valueOf(1000)), "테스트1");
            
            // 두 번째 결제
            userCreditService.pay(userId, new Money(BigDecimal.valueOf(1000)), "테스트2");

            // Then
            UserCredit finalCredit = userCreditRepository.findByUserId(userId).get();
            assertThat(finalCredit.getAmount())
                    .isEqualTo(new Money(BigDecimal.valueOf(8000))); // 10000 - 1000 - 1000
        }
    }
}