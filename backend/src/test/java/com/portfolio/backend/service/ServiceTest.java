package com.portfolio.backend.service;

import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import com.portfolio.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceTest {

    @MockBean
    private S3Client s3Client;

    @Autowired
    protected UserRepository userRepository;

    protected User adminUser;
    protected User user;

    @BeforeAll
    void initTest() {
        adminUser = userRepository.save(UserTestFixtures.createAdminUser());
        user = userRepository.save(UserTestFixtures.createUser());
    }

    @AfterAll
    void cleanUp() {
        userRepository.deleteAll();
    }
}
