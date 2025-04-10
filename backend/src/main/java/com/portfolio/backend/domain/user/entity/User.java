package com.portfolio.backend.domain.user.entity;

import com.portfolio.backend.common.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String nickname;

    @Column(nullable = false)
    private Oauth2ProviderType provider;

    @Column(updatable = false, nullable = false, unique = true)
    private String providerId;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @Builder
    public User(String email, String nickname, Oauth2ProviderType provider, String providerId, String profileImageUrl, RoleType role) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        validation();
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
