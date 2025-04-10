package com.portfolio.backend.domain.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 대시보드 정보를 저장하는 엔티티
 * 프로필 정보, 소개 내용, 외부 링크 등을 포함
 */
@Entity
@Table(name = "dashboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "velog_url")
    private String velogUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "other_url")
    private String otherUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * 생성 전 초기화 메서드
     */
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 업데이트 전 초기화 메서드
     */
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
