package com.bullscows.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在线用户会话实体类
 */
@Entity
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Token哈希
     */
    @Column(nullable = false, length = 100)
    private String tokenHash;
    
    /**
     * IP地址
     */
    @Column(length = 50)
    private String ipAddress;
    
    /**
     * 浏览器信息
     */
    @Column(columnDefinition = "TEXT")
    private String userAgent;
    
    /**
     * 登录时间
     */
    @Column(nullable = false)
    private LocalDateTime loginAt;
    
    /**
     * 最后活跃时间
     */
    @Column(nullable = false)
    private LocalDateTime lastActiveAt;
    
    /**
     * 过期时间
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        if (loginAt == null) {
            loginAt = LocalDateTime.now();
        }
        if (lastActiveAt == null) {
            lastActiveAt = LocalDateTime.now();
        }
    }
}
