package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.domain.user.entity.UserCredit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    Optional<UserCredit> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserCredit uc WHERE uc.user.id = :userId")
    Optional<UserCredit> findLockedByUserId(Long userId);
}
