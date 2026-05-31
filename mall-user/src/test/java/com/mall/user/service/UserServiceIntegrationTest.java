package com.mall.user.service;

import com.mall.user.dto.UserLoginDTO;
import com.mall.user.dto.UserRegisterDTO;
import com.mall.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterAndLogin() {
        // 测试注册
        User user = new User();
        user.setUsername("testuser2");
        user.setPassword("password123");
        user.setPhone("13800138001");
        
        boolean registerResult = userService.register(user);
        assert registerResult;
        
        // 测试登录
        User loginResult = userService.login("testuser2", "password123");
        assert loginResult != null;
        assert loginResult.getUsername().equals("testuser2");
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138002");
        
        boolean updateResult = userService.updateUser(user);
        assert updateResult;
    }
}