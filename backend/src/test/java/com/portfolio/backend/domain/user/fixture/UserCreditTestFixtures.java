package com.portfolio.backend.domain.user.fixture;

import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.entity.UserCredit;

public class UserCreditTestFixtures {

    public static UserCredit createUserCredit(User user) {
        return new UserCredit(user);
    }
}
