package com.bullscows.repository;

import com.bullscows.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 排名Repository
 */
@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    
    /**
     * 根据用户ID和排名类型查找
     */
    Optional<Ranking> findByUserIdAndRankingType(Long userId, Ranking.RankingType rankingType);
    
    /**
     * 根据排名类型获取排名列表（按位置排序）
     */
    List<Ranking> findByRankingTypeOrderByRankPositionAsc(Ranking.RankingType rankingType);
    
    /**
     * 获取排名前N名
     */
    @Query("SELECT r FROM Ranking r WHERE r.rankingType = :type ORDER BY r.rankPosition ASC LIMIT :limit")
    List<Ranking> findTopNByRankingType(Ranking.RankingType type, int limit);
    
    /**
     * 统计排名总数
     */
    long countByRankingType(Ranking.RankingType rankingType);
    
    /**
     * 删除用户的排名记录
     */
    void deleteByUserId(Long userId);
    
    /**
     * 根据排名类型获取排名列表（按最佳成绩升序）
     */
    List<Ranking> findByRankingTypeOrderByBestScoreAsc(Ranking.RankingType rankingType);
}
