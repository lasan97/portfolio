package com.portfolio.backend.service.user;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.user.dto.UserServiceResponse.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Profile getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 사용자가 존재하지 않습니다."));

        return new Profile(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl(), user.getRole());
    }
}
