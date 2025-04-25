package com.portfolio.backend.service.user;

import com.portfolio.backend.config.TestConfig;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestConfig.class})
class UserCreditServiceIntegrationTest {

    @Autowired
    private UserCreditService userCreditService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Test
    @DisplayName("크레딧 증가 시 잔액이 정상적으로 늘어난다")
    void shouldIncreaseUserCredit() {
        // Given
        User user = new User("test@example.com", "테스트사용자", Oauth2ProviderType.GITHUB, "1", null, RoleType.USER);
        userRepository.save(user);

        UserCredit userCredit = new UserCredit(user);
        userCreditRepository.save(userCredit);

        Money initialAmount = userCredit.getAmount();
        Money increaseAmount = new Money(BigDecimal.valueOf(10000));

        // When
        userCreditService.increase(user.getId(), increaseAmount);

        // Then
        UserCredit updatedCredit = userCreditRepository.findByUserId(user.getId()).get();
        assertThat(updatedCredit.getAmount()).isEqualTo(initialAmount.add(increaseAmount));
    }
}
