package com.bullscows.repository;

import com.bullscows.entity.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 练习记录Repository
 */
@Repository
public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {
    
    /**
     * 根据用户ID查找练习记录（分页）
     */
    Page<PracticeRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID查找练习记录（全部）
     */
    List<PracticeRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 统计用户练习记录数
     */
    long countByUserId(Long userId);
    
    /**
     * 查找参与排名的记录
     */
    List<PracticeRecord> findByParticipatedRankingTrue();
    
    /**
     * 今日练习次数
     */
    long countByCreatedAtAfter(java.time.LocalDateTime startOfDay);
}
