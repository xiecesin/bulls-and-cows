package com.bullscows.repository;

import com.bullscows.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户会话Repository
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    /**
     * 根据Token哈希查找会话
     */
    Optional<UserSession> findByTokenHash(String tokenHash);
    
    /**
     * 查找用户的活跃会话
     */
    List<UserSession> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);
    
    /**
     * 查找所有活跃会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt > :now ORDER BY s.lastActiveAt DESC")
    List<UserSession> findAllActiveSessions(LocalDateTime now);
    
    /**
     * 统计在线用户数
     */
    @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.expiresAt > :now")
    long countOnlineUsers(LocalDateTime now);
    
    /**
     * 删除用户的所有会话
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除过期会话
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
    
    /**
     * 更新最后活跃时间
     */
    @Query("UPDATE UserSession s SET s.lastActiveAt = :now WHERE s.tokenHash = :tokenHash")
    void updateLastActiveTime(String tokenHash, LocalDateTime now);
}
