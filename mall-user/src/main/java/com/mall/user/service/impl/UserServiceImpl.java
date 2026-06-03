package com.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.exception.BusinessException;
import com.mall.common.utils.BCryptUtil;
import com.mall.common.utils.RedisUtils;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import com.mall.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisUtils redisUtils;
    @Override
    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException(400, "用户名或密码不能为空");
        }
        
        String failedKey = "login:failed:"+username;;

        Object failedCountObj = redisUtils.get(failedKey);
        int failedCount = 0;
        if (failedCountObj != null) {
            failedCount = Integer.parseInt(failedCountObj.toString());
        }
        if (failedCount >= 5){
            long remainingTime = redisUtils.getExpire(failedKey);
            throw new BusinessException(429, "密码错误次数太多，请" + remainingTime + "稍后再试");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);

        if (user == null){
            recordLoginFailure(failedKey, failedCount);
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0){
            throw new BusinessException(403, "用户被禁用");
        }

        if (!BCryptUtil.matches(password, user.getPassword())){
            recordLoginFailure(failedKey, failedCount);
            throw new BusinessException(401, "用户名或密码错误");
        }

        redisUtils.del(failedKey);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user) {
        // 检查用户名是否存在
        if (checkUsernameExists(user.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 检查手机号是否存在
        if (checkPhoneExists(user.getPhone())) {
            throw new BusinessException(400, "手机号已注册");
        }
        
        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        user.setPassword(BCryptUtil.encode(user.getPassword()));
        return this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        User existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 如果修改了手机号，检查新手机号是否已被使用
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(existUser.getPhone())) {
            if (checkPhoneExists(user.getPhone())) {
                throw new BusinessException(400, "手机号已被使用");
            }
        }
        
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 验证原密码
        if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        
        user.setPassword(BCryptUtil.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockUser(Long userId, boolean locked) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        user.setStatus(locked ? 0 : 1);
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return this.count(wrapper) > 0;
    }

    /*
    * 记录登陆失败次数
     */
    private void recordLoginFailure(String key, int currentCount) {
        if (currentCount == 0) {
            redisUtils.set(key, 1, 60 * 15);
        } else {
            redisUtils.incr(key, 1);
        }
    }
}