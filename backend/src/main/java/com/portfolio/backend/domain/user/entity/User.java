package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.domain.common.entity.AggregateRoot;
import com.portfolio.backend.domain.user.event.UserCreatedEvent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Oauth2ProviderType provider;

    @Column(updatable = false, nullable = false, unique = true)
    private String providerId;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String nickname, Oauth2ProviderType provider, String providerId, String profileImageUrl, RoleType role) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        validation();

        registerEvent(UserCreatedEvent.builder()
                .user(this)
                .transactionDateTime(LocalDateTime.now())
                .build());
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    private void validation() {
        if(provider == null) {
            throw new DomainException("연동 채널은 null일 수 없습니다.");
        }
        if(providerId == null || providerId.isBlank()) {
            throw new DomainException("연동 아이디는 비어있을 수 없습니다.");
        }
        if(role == null) {
            throw new DomainException("권한은 null일 수 없습니다.");
        }
    }
}
