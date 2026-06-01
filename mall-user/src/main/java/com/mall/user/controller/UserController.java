package com.mall.user.controller;

import com.mall.common.response.Result;
import com.mall.user.dto.UserInfoDTO;
import com.mall.user.dto.UserLoginDTO;
import com.mall.user.dto.UserRegisterDTO;
import com.mall.user.dto.UserUpdateDTO;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<UserInfoDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        UserInfoDTO userInfoDTO = convertToDTO(user);
        return Result.success(userInfoDTO);
    }

    @PostMapping("/register")
    public Result<UserInfoDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        boolean success = userService.register(user);
        if (success) {
            User registeredUser = userService.login(registerDTO.getUsername(), registerDTO.getPassword());
            UserInfoDTO userInfoDTO = convertToDTO(registeredUser);
            return Result.success(userInfoDTO);
        } else {
            return Result.error(500, "注册失败");
        }
    }

    @GetMapping("/info/{id}")
    public Result<UserInfoDTO> getUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        UserInfoDTO userInfoDTO = convertToDTO(user);
        return Result.success(userInfoDTO);
    }

    @GetMapping("/current")
    public Result<UserInfoDTO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        UserInfoDTO userInfoDTO = convertToDTO(user);
        return Result.success(userInfoDTO);
    }

    @PutMapping("/info")
    public Result<UserInfoDTO> updateUserInfo(@RequestHeader("X-User-Id") Long userId,
                                              @Valid @RequestBody UserUpdateDTO updateDTO) {
        User user = new User();
        BeanUtils.copyProperties(updateDTO, user);
        user.setId(userId);
        boolean success = userService.updateUser(user);
        if (success) {
            User updatedUser = userService.getById(userId);
            UserInfoDTO userInfoDTO = convertToDTO(updatedUser);
            return Result.success(userInfoDTO);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    @GetMapping("/check/phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.checkPhoneExists(phone);
        return Result.success(exists);
    }

    @PutMapping("/lock/{userId}")
    public Result<Void> lockUser(@PathVariable Long userId) {
        userService.lockUser(userId, true);
        return Result.success();
    }

    @PutMapping("/unlock/{userId}")
    public Result<Void> unlockUser(@PathVariable Long userId) {
        userService.lockUser(userId, false);
        return Result.success();
    }

    private UserInfoDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}