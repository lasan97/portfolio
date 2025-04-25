package com.portfolio.backend.service.user;

import com.portfolio.backend.common.TestFixtures;
import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreditServiceTest {

    @Mock
    private UserCreditRepository userCreditRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private UserCreditService userCreditService;

    @Nested
    @DisplayName("크레딧 증가 시")
    class IncreaseTest {

        @Test
        @DisplayName("사용자가 존재하고 크레딧이 존재하면 금액을 증가시킬 수 있다")
        void shouldIncreaseAmountWhenUserAndCreditExist() {
            // Given
            Long userId = TestFixtures.USER_ID_1;
            User user = TestFixtures.createRegularUser();
            UserCredit userCredit = new UserCredit(user);
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userCreditRepository.findByUserId(userId)).thenReturn(Optional.of(userCredit));

            // When
            userCreditService.increase(userId, new UserCreditServiceRequest.Increase(amount));

            // Then
            verify(userRepository).findById(userId);
            verify(userCreditRepository).findByUserId(userId);
            verify(eventPublisher).publishEventsFrom(userCredit);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Given
            Long userId = 999L;
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> userCreditService.increase(userId, new UserCreditServiceRequest.Increase(amount)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");

            verify(userRepository).findById(userId);
            verify(userCreditRepository, never()).findByUserId(any());
            verify(eventPublisher, never()).publishEventsFrom(any());
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // Given
            Long userId = TestFixtures.USER_ID_1;
            User user = TestFixtures.createRegularUser();
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userCreditRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> userCreditService.increase(userId, new UserCreditServiceRequest.Increase(amount)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");

            verify(userRepository).findById(userId);
            verify(userCreditRepository).findByUserId(userId);
            verify(eventPublisher, never()).publishEventsFrom(any());
        }
    }

    @Nested
    @DisplayName("크레딧 감소 시")
    class DecreaseTest {

        @Test
        @DisplayName("사용자가 존재하고 크레딧이 존재하면 금액을 감소시킬 수 있다")
        void shouldDecreaseAmountWhenUserAndCreditExist() {
            // Given
            Long userId = TestFixtures.USER_ID_1;
            User user = TestFixtures.createRegularUser();
            UserCredit userCredit = new UserCredit(user);
            // 먼저 금액 추가
            userCredit.add(new Money(BigDecimal.valueOf(20000)));
            // 이벤트 초기화
            userCredit.clearDomainEvents();
            
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userCreditRepository.findByUserId(userId)).thenReturn(Optional.of(userCredit));

            // When
            userCreditService.decrease(userId, amount);

            // Then
            verify(userRepository).findById(userId);
            verify(userCreditRepository).findByUserId(userId);
            verify(eventPublisher).publishEventsFrom(userCredit);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Given
            Long userId = 999L;
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> userCreditService.decrease(userId, amount))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("해당 사용자가 존재하지 않습니다");

            verify(userRepository).findById(userId);
            verify(userCreditRepository, never()).findByUserId(any());
            verify(eventPublisher, never()).publishEventsFrom(any());
        }

        @Test
        @DisplayName("크레딧이 존재하지 않으면 예외가 발생한다")
        void shouldThrowExceptionWhenCreditDoesNotExist() {
            // Given
            Long userId = TestFixtures.USER_ID_1;
            User user = TestFixtures.createRegularUser();
            Money amount = new Money(BigDecimal.valueOf(10000));

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userCreditRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // When, Then
            assertThatThrownBy(() -> userCreditService.decrease(userId, amount))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("지갑이 존재하지 않습니다");

            verify(userRepository).findById(userId);
            verify(userCreditRepository).findByUserId(userId);
            verify(eventPublisher, never()).publishEventsFrom(any());
        }
    }
}
