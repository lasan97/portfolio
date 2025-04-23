package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.domain.user.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {
}
