package com.bullscows.service;

import com.bullscows.config.JwtUtils;
import com.bullscows.dto.*;
import com.bullscows.entity.User;
import com.bullscows.entity.UserSession;
import com.bullscows.repository.UserRepository;
import com.bullscows.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    /**
     * 用户注册
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return Map.of("success", false, "message", "用户名已存在");
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> existingEmail = userRepository.findByEmail(request.getEmail());
            if (existingEmail.isPresent()) {
                return Map.of("success", false, "message", "邮箱已被使用");
            }
        }
        
        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .email(request.getEmail())
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .oauthProvider(User.OAuthProvider.LOCAL)
                .build();
        
        user = userRepository.save(user);
        
        // 生成 Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        // 保存会话
        saveSession(user.getId(), token, "127.0.0.1", "System");
        
        return Map.of(
            "success", true,
            "message", "注册成功",
            "data", buildAuthResponse(token, refreshToken, user)
        );
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public Map<String, Object> login(LoginRequest request, String ipAddress, String userAgent) {
        // 查找用户
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }
        
        User user = userOpt.get();
        
        // 检查是否是第三方登录用户
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Map.of("success", false, "message", "请使用第三方登录");
        }
        
        // 检查用户状态
        if (user.getStatus() == User.Status.BANNED) {
            return Map.of("success", false, "message", "账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }
        
        // 生成 Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        // 保存会话
        saveSession(user.getId(), token, ipAddress, userAgent);
        
        return Map.of(
            "success", true,
            "message", "登录成功",
            "data", buildAuthResponse(token, refreshToken, user)
        );
    }
    
    /**
     * 刷新 Token
     */
    @Transactional
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            return Map.of("success", false, "message", "无效的 Refresh Token");
        }
        
        if (jwtUtils.isTokenExpired(refreshToken)) {
            return Map.of("success", false, "message", "Refresh Token 已过期");
        }
        
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }
        
        User user = userOpt.get();
        
        if (user.getStatus() == User.Status.BANNED) {
            return Map.of("success", false, "message", "账号已被禁用");
        }
        
        // 生成新 Token
        String newToken = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        return Map.of(
            "success", true,
            "message", "Token 刷新成功",
            "data", buildAuthResponse(newToken, newRefreshToken, user)
        );
    }
    
    /**
     * 退出登录
     */
    @Transactional
    public void logout(String token) {
        String tokenHash = hashToken(token);
        userSessionRepository.findByTokenHash(tokenHash).ifPresent(session -> {
            userSessionRepository.delete(session);
        });
    }
    
    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            Optional<User> existingEmail = userRepository.findByEmail(request.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(userId)) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 删除所有会话，强制重新登录
        userSessionRepository.deleteByUserId(userId);
    }
    
    /**
     * 管理员重置用户密码
     */
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 删除所有会话
        userSessionRepository.deleteByUserId(userId);
    }
    
    /**
     * 构建认证响应
     */
    private AuthResponse buildAuthResponse(String token, String refreshToken, User user) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getExpirationMs() / 1000)
                .user(AuthResponse.UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
    
    /**
     * 保存会话
     */
    private void saveSession(Long userId, String token, String ipAddress, String userAgent) {
        UserSession session = UserSession.builder()
                .userId(userId)
                .tokenHash(hashToken(token))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getExpirationMs() / 1000))
                .build();
        userSessionRepository.save(session);
    }
    
    /**
     * Token 哈希（用于存储）
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
