package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreditService {

    private final UserCreditRepository userCreditRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void decrease(Long userId, Money amount) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));
        UserCredit credit = userCreditRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));

        credit.subtract(amount);

        eventPublisher.publishEventsFrom(credit);
    }

    @Transactional
    public void increase(Long userId, UserCreditServiceRequest.Increase request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));
        UserCredit credit = userCreditRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));

        credit.add(request.amount());

        eventPublisher.publishEventsFrom(credit);
    }
}
