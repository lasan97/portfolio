package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.domain.user.entity.UserCreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCreditHistoryRepository extends JpaRepository<UserCreditHistory, Long> {
}
