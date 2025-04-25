package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {

    Optional<UserCredit> findByUserId(Long userId);

    Long user(User user);
}
