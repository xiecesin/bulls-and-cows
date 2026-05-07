package com.bullscows.repository;

import com.bullscows.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据用户名检查是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据第三方ID和来源查找用户
     */
    Optional<User> findByOauthProviderAndOauthId(User.OAuthProvider provider, String oauthId);
    
    /**
     * 根据昵称模糊搜索
     */
    List<User> findByNicknameContaining(String nickname);
    
    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(User.Status status);
    
    /**
     * 查找创建时间之后的用户
     */
    List<User> findByCreatedAtAfter(LocalDateTime time);
}
