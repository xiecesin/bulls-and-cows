package com.bullscows.controller;

import com.bullscows.config.JwtAuthFilter;
import com.bullscows.dto.UpdatePasswordRequest;
import com.bullscows.dto.UpdateUserRequest;
import com.bullscows.entity.User;
import com.bullscows.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final AuthService authService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        User user = authService.getCurrentUser(principal.username());
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "nickname", user.getNickname(),
            "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "email", user.getEmail() != null ? user.getEmail() : "",
            "role", user.getRole().name()
        ));
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateCurrentUser(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @RequestBody UpdateUserRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        try {
            User user = authService.updateUser(principal.userId(), request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "更新成功",
                "data", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "nickname", user.getNickname(),
                    "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                    "email", user.getEmail() != null ? user.getEmail() : ""
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @Valid @RequestBody UpdatePasswordRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录"));
        }
        try {
            authService.updatePassword(principal.userId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
