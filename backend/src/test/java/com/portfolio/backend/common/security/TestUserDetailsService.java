package com.portfolio.backend.common.security;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String providerId) throws UsernameNotFoundException {
        User user = userRepository.findByProviderAndProviderId(Oauth2ProviderType.GITHUB, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("TestUser NotFound"));

        UserImpl userImpl = new UserImpl(user.getId(), user.getEmail(), user.getRole());

        return new UserDetailsImpl(userImpl);
    }
}
