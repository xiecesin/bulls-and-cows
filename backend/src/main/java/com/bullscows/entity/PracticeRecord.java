package com.bullscows.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 练习记录实体类
 */
@Entity
@Table(name = "practice_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 目标数字
     */
    @Column(nullable = false, length = 10)
    private String secretNumber;
    
    /**
     * 猜测次数
     */
    @Column(nullable = false)
    private Integer guessCount;
    
    /**
     * 用时（毫秒）
     */
    private Long timeSpentMs;
    
    /**
     * 是否允许重复数字
     */
    @Column(nullable = false)
    private Boolean allowDuplicates;
    
    /**
     * 是否参与排名
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean participatedRanking = false;
    
    /**
     * 排名类型: GUESS_COUNT / TIME
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RankingType rankingType;
    
    /**
     * 游戏结果: WIN / QUIT
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GameResult gameResult;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * 排名类型枚举
     */
    public enum RankingType {
        GUESS_COUNT,
        TIME
    }
    
    /**
     * 游戏结果枚举
     */
    public enum GameResult {
        WIN,
        QUIT
    }
}
