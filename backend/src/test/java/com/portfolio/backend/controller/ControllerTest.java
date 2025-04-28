package com.portfolio.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.fixture.UserTestFixtures;
import com.portfolio.backend.domain.user.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.UnsupportedEncodingException;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTest {

    @MockBean
    private S3Client s3Client;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    protected MockMvc mockMvc;

    protected User adminUser;
    protected User user;

    @BeforeAll
    void initTest() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))  // UTF-8 필터 추가
                .build();

        adminUser = userRepository.save(UserTestFixtures.createAdminUser());
        user = userRepository.save(UserTestFixtures.createUser());
    }

    @AfterAll
    void cleanUp() {
        userRepository.deleteAll();
    }

    @SneakyThrows
    protected  <T> T resultActionsTo(ResultActions resultActions, Class<T> clazz) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, clazz);
    }

    @SneakyThrows
    protected <T> List<T> resultActionsToList(ResultActions resultActions, Class<T> clazz) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
