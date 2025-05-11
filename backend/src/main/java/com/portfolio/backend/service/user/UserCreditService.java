package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.common.util.RetryUtils;
import com.portfolio.backend.domain.common.value.Money;
import com.portfolio.backend.domain.user.entity.UserCredit;
import com.portfolio.backend.domain.user.repository.UserCreditHistoryRepository;
import com.portfolio.backend.domain.user.repository.UserCreditRepository;
import com.portfolio.backend.service.user.dto.UserCreditServiceMapper;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.Get;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.GetHistoryPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCreditService {

    private final UserCreditRepository userCreditRepository;
    private final EventPublisher eventPublisher;
    private final UserCreditHistoryRepository userCreditHistoryRepository;

    private final UserCreditServiceMapper userCreditServiceMapper;

    @Transactional(readOnly = true)
    public Get getCurrentCredit(Long userId) {
        UserCredit credit = getCreditByUserId(userId);
        return userCreditServiceMapper.toGet(credit);
    }

    @Transactional
    public void pay(Long userId, Money amount, String description) {
        RetryUtils.executeWithRetry(() -> {
            UserCredit credit = getLockedCreditByUserId(userId);
            credit.subtract(amount, description);
            eventPublisher.publishDomainEventsFrom(credit);
            return null;
        }, "UserCredit Pay");
    }

    @Transactional
    public void refund(Long userId, Money amount, String description) {
        RetryUtils.executeWithRetry(() -> {
            UserCredit credit = getLockedCreditByUserId(userId);
            credit.add(amount, description);
            eventPublisher.publishDomainEventsFrom(credit);
            return null;
        }, "UserCredit Refund");
    }

    @Transactional
    public void increase(Long userId, UserCreditServiceRequest.Increase request) {
        RetryUtils.executeWithRetry(() -> {
            UserCredit credit = getLockedCreditByUserId(userId);
            credit.add(request.amount());
            eventPublisher.publishDomainEventsFrom(credit);
            return null;
        }, "UserCredit Increase");
    }

    private UserCredit getCreditByUserId(Long userId) {
        return userCreditRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));
    }

    private UserCredit getLockedCreditByUserId(Long userId) {
        return userCreditRepository.findLockedByUserId(userId)
                .orElseThrow(() -> new DomainException("지갑이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Page<GetHistoryPage> getHistoryPage(Long userId, Pageable pageable) {
        UserCredit credit = getCreditByUserId(userId);

        return userCreditHistoryRepository.findAllByUserCreditId(credit.getId(), pageable)
                .map(userCreditServiceMapper::toGetHistoryPage);
    }
}
