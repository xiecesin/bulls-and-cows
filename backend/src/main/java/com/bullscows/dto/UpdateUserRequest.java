package com.bullscows.dto;

import lombok.Data;

/**
 * 更新用户信息请求 DTO
 */
@Data
public class UpdateUserRequest {
    
    private String nickname;
    
    private String avatarUrl;
    
    private String email;
}
