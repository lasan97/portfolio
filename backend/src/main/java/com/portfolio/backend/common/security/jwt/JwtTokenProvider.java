package com.portfolio.backend.common.security.jwt;

import com.portfolio.backend.common.security.UserImpl;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.common.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long tokenValidityInMilliseconds;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(User user) {
        return createToken(user.getId(), user.getEmail(), user.getRole());
    }
    
    public String createToken(Long userId) {
        // 유저 ID만 있는 경우 기본 ROLE_USER로 생성
        return createToken(userId, "user_" + userId + "@example.com", RoleType.USER);
    }
    
    private String createToken(Long userId, String email, RoleType role) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenValidityInMilliseconds, ChronoUnit.MILLIS);

        Map<String, Object> claims = Map.of(
            "id", userId,
            "role", role.name()
        );

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        var claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long id = claims.get("id", Long.class);
        String email = claims.getSubject();
        RoleType role = RoleType.valueOf(claims.get("role", String.class));

        UserImpl user = new UserImpl(id, email, role);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid");
        }
        return false;
    }
}