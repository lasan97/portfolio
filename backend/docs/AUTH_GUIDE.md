# 인증 가이드

이 문서는 포트폴리오 백엔드 프로젝트의 인증 시스템과 관련된 상세 정보와 구현 가이드를 제공합니다.

## 목차
1. [인증 아키텍처 개요](#인증-아키텍처-개요)
2. [OAuth2 인증 흐름](#oauth2-인증-흐름)
3. [JWT 토큰 관리](#jwt-토큰-관리)
4. [보안 설정](#보안-설정)
5. [인증 관련 구현 가이드](#인증-관련-구현-가이드)
6. [예외 처리](#예외-처리)

## 인증 아키텍처 개요

포트폴리오 백엔드 애플리케이션은 다음과 같은 인증 메커니즘을 사용합니다:

1. **OAuth2 기반 로그인**: 사용자는 GitHub OAuth2 제공자를 통해 인증합니다.
2. **JWT 토큰 발급**: 인증 성공 시 JWT 토큰을 발급하여 사용자에게 제공합니다.
3. **Stateless 인증**: 토큰 기반의 Stateless 인증을 사용하여 세션을 유지하지 않습니다.

## OAuth2 인증 흐름

### 1. GitHub OAuth2 인증 프로세스

```
+--------+                               +---------------+
|        |--(1)- Redirect to GitHub ---->|               |
|        |                               |               |
|        |<-(2)---- Auth Code -----------|    GitHub     |
|        |                               |               |
|        |                               |               |
|        |                               +---------------+
| Client |
|        |                               +---------------+
|        |--(3)- Auth Code ------------->|               |
|        |                               |  Backend API  |
|        |<-(4)---- JWT Token -----------|               |
|        |                               |               |
+--------+                               +---------------+
```

1. 클라이언트가 사용자를 GitHub 인증 페이지로 리디렉션합니다.
2. 사용자가 인증하면 GitHub는 인증 코드와 함께 리디렉션합니다.
3. 클라이언트는 이 인증 코드를 백엔드 API에 보냅니다.
4. 백엔드는 GitHub API를 통해 인증 코드를 검증하고, 사용자 정보를 가져온 후 JWT 토큰을 발급합니다.

### 2. 클라이언트 측 설정

프론트엔드 애플리케이션은 GitHub OAuth 앱의 클라이언트 ID와 리디렉션 URL을 구성해야 합니다. GitHub OAuth 인증 URL은 다음과 같습니다:

```
https://github.com/login/oauth/authorize?client_id={CLIENT_ID}&redirect_uri={REDIRECT_URI}&scope=user:email
```

### 3. 백엔드 측 설정

`application.yml` 파일에서 OAuth2 클라이언트 설정:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
```

## JWT 토큰 관리

### 1. 토큰 구조

JWT 토큰은 다음과 같은 정보를 포함합니다:

**Header**:
```json
{
  "alg": "HS256", 
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "user@example.com",
  "id": 1,
  "role": "USER",
  "iat": 1683025200,
  "exp": 1683111600
}
```

- `sub`: 토큰 제목 (사용자 이메일)
- `id`: 사용자 ID
- `role`: 사용자 역할
- `iat`: 토큰 발급 시간
- `exp`: 토큰 만료 시간

### 2. 토큰 발급 및 검증

**토큰 발급**:
백엔드는 `JwtTokenProvider` 클래스를 사용하여 JWT 토큰을 생성합니다:

```java
// 토큰 생성
String token = jwtTokenProvider.createToken(user);
```

**토큰 검증**:
요청에 포함된 JWT 토큰은 `JwtAuthenticationFilter`에서 검증됩니다:

```java
// 요청 헤더에서 토큰 추출
String token = jwtTokenProvider.resolveToken(request);

// 토큰 검증
if (token != null && jwtTokenProvider.validateToken(token)) {
    Authentication auth = jwtTokenProvider.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(auth);
}
```

### 3. 토큰 설정

`application.yml` 파일에서 JWT 토큰 관련 설정:

```yaml
jwt:
  secret: ${JWT_SECRET:12345678901234567890123456789012}
  expiration: 86400000 # 24시간
```

- `jwt.secret`: 토큰 서명에 사용되는 비밀키 (최소 32자리 권장)
- `jwt.expiration`: 토큰 만료 시간 (밀리초 단위)

## 보안 설정

### 1. Spring Security 설정

보안 설정은 `SecurityConfig` 클래스에서 구성됩니다:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**", "/login/**", "/h2-console/**").permitAll()
                .requestMatchers("/api/dashboard/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 2. CORS 설정

Cross-Origin Resource Sharing 설정:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:8080"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 3. JWT 인증 필터

JWT 인증을 처리하는 필터:

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // JWT 토큰이 유효하면 SecurityContext에 인증 정보 설정
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

## 인증 관련 구현 가이드

### 1. 새로운 OAuth2 제공자 추가

GitHub 외의 다른 OAuth2 제공자를 추가하려면:

1. `Oauth2ProviderType` enum에 새 제공자 추가:

```java
public enum Oauth2ProviderType {
    GOOGLE("google"),
    FACEBOOK("facebook"),
    GITHUB("github"),
    NEW_PROVIDER("new_provider");  // 새 제공자 추가
    
    private final String registrationId;
    
    Oauth2ProviderType(String registrationId) {
        this.registrationId = registrationId;
    }
    
    // ...
}
```

2. `application.yml`에 새 OAuth2 클라이언트 등록:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          new_provider:
            client-id: ${NEW_PROVIDER_CLIENT_ID}
            client-secret: ${NEW_PROVIDER_CLIENT_SECRET}
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            scope: profile, email
```

3. OAuth2 서비스에 새 제공자에 대한 처리 추가:

```java
public OAuth2ServiceResponse.TokenResponse processNewProviderLogin(String code) {
    // 새 제공자 API를 통해 인증 코드 검증 및 사용자 정보 조회
    // 사용자 정보를 바탕으로 JWT 토큰 생성 및 반환
}
```

### 2. 권한 제어

역할 기반 접근 제어(RBAC)는 다음과 같은 방법으로 구현됩니다:

1. 컨트롤러 메서드에 `@PreAuthorize` 어노테이션 사용:

```java
@PutMapping
@PreAuthorize("hasRole('ADMIN')")
public Long updateDashboard(@Valid @RequestBody Update request, 
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return dashboardService.updateDashboard(request, userDetails);
}
```

2. 프로그래밍 방식의 권한 검사:

```java
if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
    throw new AccessDeniedException("관리자 권한이 필요합니다.");
}
```

### 3. 현재 인증된 사용자 참조

컨트롤러 메서드에서 현재 인증된 사용자 정보를 참조하는 방법:

```java
@GetMapping("/me")
public UserServiceResponse.Profile getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.getUserProfile(userDetails.getUser().id());
}
```

## 예외 처리

인증 관련 예외 처리는 `GlobalExceptionHandler` 클래스에서 담당합니다:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "접근 권한이 없습니다.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "인증에 실패했습니다.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    // 기타 예외 처리...
}
```

주요 인증 관련 예외:

- `AccessDeniedException`: 권한 부족으로 접근 거부됨 (403)
- `AuthenticationException`: 인증 실패 (401)
- `InvalidTokenException`: 유효하지 않은 토큰 (401)
- `TokenExpiredException`: 만료된 토큰 (401)
