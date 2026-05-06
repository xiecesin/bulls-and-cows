package com.bullscows.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排名实体类
 */
@Entity
@Table(name = "rankings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "ranking_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ranking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 排名类型: GUESS_COUNT / TIME
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RankingType rankingType;
    
    /**
     * 排名位置
     */
    @Column(nullable = false)
    private Integer rankPosition;
    
    /**
     * 最佳成绩
     */
    @Column(nullable = false)
    private Integer bestScore;
    
    /**
     * 来源记录ID
     */
    @Column(nullable = false)
    private Long recordId;
    
    /**
     * 达成时间
     */
    private LocalDateTime achievedAt;
    
    @PrePersist
    protected void onCreate() {
        if (achievedAt == null) {
            achievedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 排名类型枚举
     */
    public enum RankingType {
        GUESS_COUNT,
        TIME
    }
}
