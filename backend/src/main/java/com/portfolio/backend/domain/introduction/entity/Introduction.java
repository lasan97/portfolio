package com.portfolio.backend.domain.introduction.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.introduction.value.ExternalLink;
import com.portfolio.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "introductions")
public class Introduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name = "introduction_external_links", joinColumns = @JoinColumn(name = "introduction_id"))
    private List<ExternalLink> externalLinks = new ArrayList<>();

    @JoinColumn(updatable = false, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private User updatedBy;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Builder
    public Introduction(String title, String content, List<ExternalLink> externalLinks, User createdBy, LocalDateTime createdAt) {

        if(createdBy == null) {
            throw new DomainException("생성자는 null일 수 없습니다.");
        }
        if(createdAt == null) {
            throw new DomainException("생성일시는 null일 수 없습니다.");
        }

        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        setExternalLinks(externalLinks);
        validation();
    }

    public void update(String title, String content, List<ExternalLink> externalLinks, User updatedBy, LocalDateTime updatedAt) {

        if(updatedBy == null) {
            throw new DomainException("수정자는 null일 수 없습니다.");
        }
        if(updatedAt == null) {
            throw new DomainException("수정일시는 null일 수 없습니다.");
        }

        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        setExternalLinks(externalLinks);
        validation();
    }

    private void validation() {
        if(title == null || title.isBlank()) {
            throw new DomainException("제목은 비어있을 수 없습니다.");
        }
        if(content == null || content.isBlank()) {
            throw new DomainException("내용은 비어있을 수 없습니다.");
        }
    }

    public List<ExternalLink> getExternalLinks() {
        return Collections.unmodifiableList(externalLinks);
    }

    private void setExternalLinks(List<ExternalLink> externalLinks) {
        this.externalLinks.clear();
        this.externalLinks.addAll(externalLinks);
    }
}
