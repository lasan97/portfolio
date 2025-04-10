package com.portfolio.backend.service.introduction;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.domain.introduction.entity.Introduction;
import com.portfolio.backend.domain.introduction.repository.IntroductionRepository;
import com.portfolio.backend.domain.introduction.value.ExternalLink;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceMapper;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceRequest.Create;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceRequest.Update;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceResponse.Get;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntroductionService {

    private final IntroductionRepository introductionRepository;
    private final IntroductionServiceMapper introductionServiceMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Get getIntroduction() {

        Optional<Introduction> response = introductionRepository.findFirstByOrderByUpdatedAtDesc();

        if(response.isEmpty()) {
            throw new ResourceNotFoundException("자기소개 정보를 찾을 수 없습니다.");
        }

        return introductionServiceMapper.toGet(response.get());
    }

    @Transactional
    public Long create(Create request, UserDetailsImpl userDetails) {
        Optional<Introduction> response = introductionRepository.findFirstByOrderByUpdatedAtDesc();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new DomainException("사용자 정보가 없습니다. 잘못된 접근입니다."));

        if(response.isPresent()) {
            throw new UnprocessableEntityException("자기소개가 이미 존재합니다.");
        }

        List<ExternalLink> externalLinks = request.externalLinks().stream().map(link ->
                new ExternalLink(link.name(), link.url(), link.logoUrl())).toList();

        Introduction introduction = introductionRepository.save(Introduction.builder()
                .title(request.title())
                .content(request.content())
                .externalLinks(externalLinks)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build());

        return introduction.getId();
    }

    @Transactional
    public Long updateIntroduction(Update request, UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new DomainException("사용자 정보가 없습니다. 잘못된 접근입니다."));
        Introduction introduction = introductionRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("자기소개 정보를 찾을 수 없습니다."));

        List<ExternalLink> externalLinks = request.externalLinks().stream().map(link ->
                new ExternalLink(link.name(), link.url(), link.logoUrl())).toList();

        introduction.update(request.title(), request.content(), externalLinks, user, LocalDateTime.now());

        return introduction.getId();
    }
}
