package com.example;

import com.example.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles({ "in_memory" })
public class UserServiceTest {
    public static final String TEST_USR_NAME = "demo";
    public static final String TEST_PASS = "password";
    public static final String EXPECT_TO_FIND_USER_BY_NAME = "Expect to find user by name ";
    public static final String EXPECTED_NO_USER_TO_BE_LOGGED_IN = "Expected no user to be logged in!";
    public static final String LOGIN_FAILED = "Login failed!";
    public static final String LOGOUT_PERFORMED_UNSUCCESSFULLY = "Logout performed unsuccessfully!";

    @Autowired
    private UserService userService;

    @Before
    public void createUserBeforeTest() {
        userService.create(new User(TEST_USR_NAME, TEST_PASS));
    }

    @Test
    public void testFindUserBySpringRepository() {
        Assert.assertNotNull(EXPECT_TO_FIND_USER_BY_NAME + TEST_USR_NAME, userService.findByUserName(TEST_USR_NAME));
    }

    @Test
    public void testFindUserByEntityManager() {
        Assert.assertNotNull(EXPECT_TO_FIND_USER_BY_NAME + TEST_USR_NAME, userService.findByUserNameEM(TEST_USR_NAME));
    }

    @Test
    public void testLoginLogout(){
        Assert.assertEquals(EXPECTED_NO_USER_TO_BE_LOGGED_IN, null, userService.getSession().getAttribute(Constants.CURRENT_USER));

        try {
            userService.login(TEST_USR_NAME, TEST_PASS);
        } catch (AuthenticationException e) {
            Assert.fail(LOGIN_FAILED);
            e.printStackTrace();
        }

        Assert.assertNotNull(LOGIN_FAILED, userService.getSession().getAttribute(Constants.CURRENT_USER));

        userService.logout();

        Assert.assertEquals(LOGOUT_PERFORMED_UNSUCCESSFULLY, null, userService.getSession().getAttribute(Constants.CURRENT_USER));
    }
}