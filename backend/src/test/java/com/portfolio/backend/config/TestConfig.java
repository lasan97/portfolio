package com.portfolio.backend.config;

import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.user.event.listener.UserCreditEventListener;
import com.portfolio.backend.domain.user.repository.UserCreditHistoryRepository;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.user.UserCreditService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableJpaAuditing
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher publisher) {
        return new DomainEventPublisher(publisher);
    }

    @Bean
    public UserCreditService userCreditService(
            UserCreditRepository userCreditRepository,
            UserRepository userRepository,
            DomainEventPublisher domainEventPublisher) {
        return new UserCreditService(userCreditRepository, userRepository, domainEventPublisher);
    }

    @Bean
    public UserCreditEventListener userCreditEventListener(UserCreditHistoryRepository userCreditHistoryRepository) {
        return new UserCreditEventListener(userCreditHistoryRepository);
    }
}
