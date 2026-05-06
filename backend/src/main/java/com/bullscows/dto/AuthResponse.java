package com.bullscows.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * 访问 Token
     */
    private String token;
    
    /**
     * Refresh Token
     */
    private String refreshToken;
    
    /**
     * Token 类型
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserDTO user;
    
    /**
     * 用户信息 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String username;
        private String nickname;
        private String avatarUrl;
        private String email;
        private String role;
    }
}
