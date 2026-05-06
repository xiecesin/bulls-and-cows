package com.bullscows.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名（登录用，唯一）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码（加密存储，第三方登录为null）
     */
    @Column(length = 255)
    private String password;
    
    /**
     * 昵称
     */
    @Column(length = 100)
    private String nickname;
    
    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatarUrl;
    
    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;
    
    /**
     * 用户角色: USER / ADMIN
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;
    
    /**
     * 用户状态: ACTIVE / BANNED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    /**
     * 第三方登录来源: LOCAL / WECHAT / GITHUB
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private OAuthProvider oauthProvider = OAuthProvider.LOCAL;
    
    /**
     * 第三方用户ID
     */
    @Column(length = 100)
    private String oauthId;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nickname == null || nickname.isEmpty()) {
            nickname = username;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 用户角色枚举
     */
    public enum Role {
        USER,
        ADMIN
    }
    
    /**
     * 用户状态枚举
     */
    public enum Status {
        ACTIVE,
        BANNED
    }
    
    /**
     * OAuth来源枚举
     */
    public enum OAuthProvider {
        LOCAL,
        WECHAT,
        GITHUB
    }
}
