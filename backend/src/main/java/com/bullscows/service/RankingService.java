package com.bullscows.service;

import com.bullscows.entity.PracticeRecord;
import com.bullscows.entity.Ranking;
import com.bullscows.entity.User;
import com.bullscows.repository.PracticeRecordRepository;
import com.bullscows.repository.RankingRepository;
import com.bullscows.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排行榜服务
 */
@Service
public class RankingService {

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private PracticeRecordRepository practiceRecordRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 更新排行榜
     */
    public Ranking updateRanking(Long userId, PracticeRecord record) {
        if (record.getGameResult() != PracticeRecord.GameResult.WIN) {
            return null;
        }

        // 检查是否是猜测次数排行榜
        if (record.getRankingType() == PracticeRecord.RankingType.GUESS_COUNT) {
            return updateGuessCountRanking(userId, record);
        }

        return null;
    }

    /**
     * 更新猜测次数排行榜
     */
    private Ranking updateGuessCountRanking(Long userId, PracticeRecord record) {
        // 获取当前用户的排名记录
        Optional<Ranking> existingRanking = rankingRepository.findByUserIdAndRankingType(
                userId, Ranking.RankingType.GUESS_COUNT);

        Ranking ranking;
        if (existingRanking.isPresent()) {
            ranking = existingRanking.get();
            // 如果新成绩更好，则更新
            if (record.getGuessCount() < ranking.getBestScore()) {
                ranking.setBestScore(record.getGuessCount());
                ranking.setRecordId(record.getId());
                ranking.setAchievedAt(LocalDateTime.now());
                ranking = rankingRepository.save(ranking);
                recalculateRankings(Ranking.RankingType.GUESS_COUNT);
            }
        } else {
            // 创建新记录
            ranking = Ranking.builder()
                    .userId(userId)
                    .rankingType(Ranking.RankingType.GUESS_COUNT)
                    .bestScore(record.getGuessCount())
                    .recordId(record.getId())
                    .achievedAt(LocalDateTime.now())
                    .rankPosition(0) // 稍后计算
                    .build();
            ranking = rankingRepository.save(ranking);
            recalculateRankings(Ranking.RankingType.GUESS_COUNT);
        }

        return ranking;
    }

    /**
     * 重新计算排行榜
     */
    private void recalculateRankings(Ranking.RankingType rankingType) {
        List<Ranking> rankings = rankingRepository.findByRankingTypeOrderByBestScoreAsc(rankingType);

        int position = 1;
        for (Ranking ranking : rankings) {
            ranking.setRankPosition(position++);
        }

        rankingRepository.saveAll(rankings);
    }

    /**
     * 获取排行榜
     */
    public List<Map<String, Object>> getRanking(Ranking.RankingType rankingType, int limit) {
        List<Ranking> rankings = rankingRepository.findByRankingTypeOrderByRankPositionAsc(rankingType);

        return rankings.stream()
                .limit(limit)
                .map(ranking -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("rank", ranking.getRankPosition());
                    item.put("userId", ranking.getUserId());

                    // 获取用户名
                    Optional<User> user = userRepository.findById(ranking.getUserId());
                    item.put("username", user.map(User::getUsername).orElse("匿名用户"));

                    item.put("score", ranking.getBestScore());
                    item.put("achievedAt", ranking.getAchievedAt());

                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的排名
     */
    public Map<String, Object> getUserRanking(Long userId, Ranking.RankingType rankingType) {
        Optional<Ranking> ranking = rankingRepository.findByUserIdAndRankingType(userId, rankingType);

        if (ranking.isEmpty()) {
            return null;
        }

        Ranking r = ranking.get();
        long totalPlayers = rankingRepository.countByRankingType(rankingType);

        Map<String, Object> result = new HashMap<>();
        result.put("rank", r.getRankPosition());
        result.put("score", r.getBestScore());
        result.put("totalPlayers", totalPlayers);
        result.put("achievedAt", r.getAchievedAt());

        return result;
    }

    /**
     * 获取用户的所有排名
     */
    public Map<String, Object> getUserAllRankings(Long userId) {
        Map<String, Object> rankings = new HashMap<>();

        for (Ranking.RankingType type : Ranking.RankingType.values()) {
            Optional<Ranking> ranking = rankingRepository.findByUserIdAndRankingType(userId, type);
            if (ranking.isPresent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("rank", ranking.get().getRankPosition());
                item.put("score", ranking.get().getBestScore());
                rankings.put(type.name(), item);
            }
        }

        return rankings;
    }
}
