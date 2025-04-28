package com.portfolio.backend.domain.user.repository;

import com.portfolio.backend.domain.user.entity.UserCreditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCreditHistoryRepository extends JpaRepository<UserCreditHistory, Long> {

    Page<UserCreditHistory> findAllByUserCreditId(Long userCreditId, Pageable pageable);
}
