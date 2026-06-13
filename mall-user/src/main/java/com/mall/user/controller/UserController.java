package com.mall.user.controller;

import com.mall.common.response.Result;
import com.mall.user.dto.*;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户服务 Controller
 *
 * <p>权限控制说明：</p>
 * <ul>
 *   <li>公开接口：登录、注册、校验用户名/手机号</li>
 *   <li>用户接口：查询/修改自己的信息、修改密码、退出登录</li>
 *   <li>管理员接口：锁定/解锁用户（需要admin角色）</li>
 * </ul>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 校验用户名是否已存在
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam @NotBlank String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    /**
     * 校验手机号是否已存在
     */
    @GetMapping("/check/phone")
    public Result<Boolean> checkPhone(@RequestParam @NotBlank String phone) {
        boolean exists = userService.checkPhoneExists(phone);
        return Result.success(exists);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserInfoDTO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        UserInfoDTO userInfoDTO = convertToDTO(user);
        return Result.success(userInfoDTO);
    }

    /**
     * 获取用户信息（管理员或本人可查看）
     */
    @GetMapping("/info/{id}")
    public Result<UserInfoDTO> getUserInfo(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        // 只能查看自己的信息，除非是管理员
        if (!id.equals(currentUserId) && !"admin".equals(currentUserRole)) {
            return Result.error(403, "无权查看其他用户信息");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        UserInfoDTO userInfoDTO = convertToDTO(user);
        return Result.success(userInfoDTO);
    }

    /**
     * 修改当前用户信息
     */
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

    /**
     * 修改密码
     *
     * <p>使用POST+请求体而非RequestParam，避免密码暴露在URL和日志中。</p>
     */
    @PostMapping("/password")
    public Result<Void> changePassword(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody ChangePasswordDTO passwordDTO) {
        userService.updatePassword(userId, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success();
    }

    /**
     * 锁定用户 - 仅管理员可操作
     */
    @PutMapping("/lock/{userId}")
    public Result<Void> lockUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        // 权限校验：仅admin角色可操作
        if (!"admin".equals(currentUserRole)) {
            log.warn("非管理员尝试锁定用户: 操作人角色={}, 目标用户ID={}", currentUserRole, userId);
            return Result.error(403, "权限不足，仅管理员可锁定用户");
        }

        userService.lockUser(userId, true);
        log.info("管理员锁定用户: userId={}", userId);
        return Result.success();
    }

    /**
     * 解锁用户 - 仅管理员可操作
     */
    @PutMapping("/unlock/{userId}")
    public Result<Void> unlockUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        // 权限校验：仅admin角色可操作
        if (!"admin".equals(currentUserRole)) {
            log.warn("非管理员尝试解锁用户: 操作人角色={}, 目标用户ID={}", currentUserRole, userId);
            return Result.error(403, "权限不足，仅管理员可解锁用户");
        }

        userService.lockUser(userId, false);
        log.info("管理员解锁用户: userId={}", userId);
        return Result.success();
    }

    // ==================== 私有方法 ====================

    private UserInfoDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
