package com.portfolio.backend.domain.user.fixture;

import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;

public class UserTestFixtures {

    public static User createAdminUser() {
        return new User("admin@email.com", "tester", Oauth2ProviderType.GITHUB, "admin", null, RoleType.ADMIN);
    }

    public static User createUser() {
        return new User("user@email.com", "tester", Oauth2ProviderType.GITHUB, "user", null, RoleType.USER);
    }
}
