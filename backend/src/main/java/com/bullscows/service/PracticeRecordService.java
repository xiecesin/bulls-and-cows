package com.bullscows.service;

import com.bullscows.entity.PracticeRecord;
import com.bullscows.repository.PracticeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 练习记录服务
 */
@Service
public class PracticeRecordService {

    @Autowired
    private PracticeRecordRepository practiceRecordRepository;

    /**
     * 保存练习记录
     */
    public PracticeRecord saveRecord(PracticeRecord record) {
        return practiceRecordRepository.save(record);
    }

    /**
     * 创建并保存练习记录
     */
    public PracticeRecord createRecord(Long userId, String secretNumber, Integer guessCount,
                                       Long timeSpentMs, Boolean allowDuplicates,
                                       PracticeRecord.GameResult gameResult) {
        PracticeRecord record = PracticeRecord.builder()
                .userId(userId)
                .secretNumber(secretNumber)
                .guessCount(guessCount)
                .timeSpentMs(timeSpentMs)
                .allowDuplicates(allowDuplicates)
                .gameResult(gameResult)
                .participatedRanking(gameResult == PracticeRecord.GameResult.WIN)
                .rankingType(PracticeRecord.RankingType.GUESS_COUNT)
                .completedAt(LocalDateTime.now())
                .build();

        return practiceRecordRepository.save(record);
    }

    /**
     * 获取用户的练习记录（分页）
     */
    public Page<PracticeRecord> getUserRecords(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return practiceRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户的所有练习记录
     */
    public List<PracticeRecord> getUserAllRecords(Long userId) {
        return practiceRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 统计用户练习记录数
     */
    public long countUserRecords(Long userId) {
        return practiceRecordRepository.countByUserId(userId);
    }

    /**
     * 获取用户今日练习次数
     */
    public long countTodayRecords(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return practiceRecordRepository.countByCreatedAtAfter(startOfDay);
    }

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        List<PracticeRecord> records = practiceRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long totalGames = records.size();
        long winGames = records.stream()
                .filter(r -> r.getGameResult() == PracticeRecord.GameResult.WIN)
                .count();
        long quitGames = totalGames - winGames;

        // 计算平均猜测次数
        double avgGuessCount = records.stream()
                .filter(r -> r.getGameResult() == PracticeRecord.GameResult.WIN)
                .mapToInt(PracticeRecord::getGuessCount)
                .average()
                .orElse(0.0);

        // 最佳成绩
        int bestGuessCount = records.stream()
                .filter(r -> r.getGameResult() == PracticeRecord.GameResult.WIN)
                .mapToInt(PracticeRecord::getGuessCount)
                .min()
                .orElse(0);

        // 计算胜率
        double winRate = totalGames > 0 ? (double) winGames / totalGames * 100 : 0.0;

        // 今日练习次数
        long todayCount = countTodayRecords(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", totalGames);
        stats.put("winGames", winGames);
        stats.put("quitGames", quitGames);
        stats.put("winRate", Math.round(winRate * 100.0) / 100.0);
        stats.put("avgGuessCount", Math.round(avgGuessCount * 100.0) / 100.0);
        stats.put("bestGuessCount", bestGuessCount);
        stats.put("todayCount", todayCount);

        return stats;
    }

    /**
     * 删除练习记录
     */
    public void deleteRecord(Long recordId) {
        practiceRecordRepository.deleteById(recordId);
    }
}
