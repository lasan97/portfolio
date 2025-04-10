package com.portfolio.backend.domain.introduction.repository;

import com.portfolio.backend.domain.introduction.entity.Introduction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntroductionRepository extends JpaRepository<Introduction, Long> {
    Optional<Introduction> findFirstByOrderByUpdatedAtDesc();
}
