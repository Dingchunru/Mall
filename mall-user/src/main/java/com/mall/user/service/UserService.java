package com.mall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.user.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(User user);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
    
    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 锁定/解锁用户
     */
    boolean lockUser(Long userId, boolean locked);
    
    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone);
}