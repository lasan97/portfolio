package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.event.DomainEventPublisher;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.service.user.dto.UserCreditServiceMapper;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCreditService {

    private final UserCreditRepository userCreditRepository;
    private final DomainEventPublisher eventPublisher;

    private final UserCreditServiceMapper userCreditServiceMapper;

    @Transactional(readOnly = true)
    public UserCreditServiceResponse.Get getCurrentCredit(Long userId) {
        UserCredit credit = userCreditRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));
        return userCreditServiceMapper.toGet(credit);
    }

    @Transactional
    public void pay(Long userId, Money amount) {
        UserCredit credit = getLockedCreditByUserId(userId);

        credit.subtract(amount);

        eventPublisher.publishEventsFrom(credit);
    }

    @Transactional
    public void increase(Long userId, UserCreditServiceRequest.Increase request) {
        UserCredit credit = getLockedCreditByUserId(userId);

        credit.add(request.amount());

        eventPublisher.publishEventsFrom(credit);
    }

    private UserCredit getLockedCreditByUserId(Long userId) {
        return userCreditRepository.findLockedByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));
    }
}
