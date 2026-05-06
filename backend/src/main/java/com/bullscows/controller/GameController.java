package com.bullscows.controller;

import com.bullscows.entity.PracticeRecord;
import com.bullscows.entity.Ranking;
import com.bullscows.model.AlgorithmModels;
import com.bullscows.model.GameState;
import com.bullscows.model.GuessResult;
import com.bullscows.model.VariantsModels;
import com.bullscows.service.GameService;
import com.bullscows.service.PracticeRecordService;
import com.bullscows.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器 - 提供 REST API
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GameController {
    
    @Autowired
    private GameService gameService;

    @Autowired
    private PracticeRecordService practiceRecordService;

    @Autowired
    private RankingService rankingService;
    
    /**
     * 开始新游戏（支持配置是否允许重复）
     */
    @PostMapping("/game/start")
    public Map<String, Object> startGame(@RequestBody(required = false) Map<String, Boolean> request) {
        boolean allowDuplicates = request != null && Boolean.TRUE.equals(request.get("allowDuplicates"));
        GameState game = gameService.startNewGame(allowDuplicates);
        String hint = allowDuplicates 
            ? "提示：可以包含重复数字，如 1123, 7777 等"
            : "提示：4位数字各不相同，如 1234, 5678 等";
        return Map.of(
            "gameId", game.getGameId(),
            "allowDuplicates", allowDuplicates,
            "message", "新游戏已开始！请输入4位数字进行猜测。",
            "hint", hint
        );
    }
    
    /**
     * 提交猜测
     */
    @PostMapping("/game/guess")
    public Map<String, Object> makeGuess(@RequestBody Map<String, String> request) {
        String gameId = request.get("gameId");
        String guess = request.get("guess");
        
        if (!gameService.isValidGuess(guess)) {
            return Map.of(
                "error", "无效的猜测格式",
                "message", "请输入4位数字，如 1234"
            );
        }
        
        GuessResult result = gameService.processGuess(gameId, guess);
        if (result == null) {
            return Map.of(
                "error", "游戏不存在或已结束",
                "message", "请先开始新游戏"
            );
        }
        
        if (result.isSuccess()) {
            return Map.of(
                "success", true,
                "guess", guess,
                "bulls", result.getBulls(),
                "cows", result.getCows(),
                "result", result.getFormattedResult(),
                "guessCount", result.getGuessCount(),
                "message", String.format("恭喜！你用了 %d 次猜出了正确答案！", result.getGuessCount()),
                "reasoning", result.getReasoning()
            );
        } else {
            return Map.of(
                "success", false,
                "guess", guess,
                "bulls", result.getBulls(),
                "cows", result.getCows(),
                "result", result.getFormattedResult(),
                "guessCount", result.getGuessCount(),
                "message", String.format("当前结果: %s，继续加油！", result.getFormattedResult()),
                "reasoning", result.getReasoning()
            );
        }
    }
    
    /**
     * 直接计算结果（无需游戏）
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculate(@RequestBody Map<String, String> request) {
        String secret = request.get("secret");
        String guess = request.get("guess");
        
        if (!gameService.isValidGuess(secret) || !gameService.isValidGuess(guess)) {
            return Map.of(
                "error", "无效的输入格式",
                "message", "请确保输入的是4位数字"
            );
        }
        
        GuessResult result = gameService.calculateWithReasoning(secret, guess);
        return Map.of(
            "secret", secret,
            "guess", guess,
            "bulls", result.getBulls(),
            "cows", result.getCows(),
            "result", result.getFormattedResult(),
            "reasoning", result.getReasoning()
        );
    }
    
    /**
     * 获取基础算法
     */
    @GetMapping("/algorithm/basic")
    public Map<String, Object> getBasicAlgorithm(
            @RequestParam(defaultValue = "java") String language) {
        if ("python".equalsIgnoreCase(language)) {
            AlgorithmModels algo = AlgorithmModels.getBasicPythonAlgorithm();
            return Map.of(
                "name", algo.getName(),
                "language", algo.getLanguage(),
                "code", algo.getCode(),
                "description", algo.getDescription(),
                "complexity", algo.getComplexity()
            );
        } else {
            AlgorithmModels algo = AlgorithmModels.getBasicJavaAlgorithm();
            return Map.of(
                "name", algo.getName(),
                "language", algo.getLanguage(),
                "code", algo.getCode(),
                "description", algo.getDescription(),
                "complexity", algo.getComplexity()
            );
        }
    }
    
    /**
     * 获取高级算法
     */
    @GetMapping("/algorithm/advanced")
    public Map<String, Object> getAdvancedAlgorithm(
            @RequestParam(defaultValue = "java") String language) {
        if ("python".equalsIgnoreCase(language)) {
            return Map.of(
                "name", "Python优化版本",
                "language", "Python",
                "description", "Python的高级实现方式",
                "note", "Python版本的高级算法与基础版类似，主要优势在于语言本身的简洁性"
            );
        } else {
            AlgorithmModels algo = AlgorithmModels.getOptimizedJavaAlgorithm();
            return Map.of(
                "name", algo.getName(),
                "language", algo.getLanguage(),
                "code", algo.getCode(),
                "description", algo.getDescription(),
                "complexity", algo.getComplexity()
            );
        }
    }
    
    /**
     * 获取所有算法
     */
    @GetMapping("/algorithms")
    public Map<String, Object> getAllAlgorithms() {
        return Map.of(
            "algorithms", List.of(
                AlgorithmModels.getBasicJavaAlgorithm(),
                AlgorithmModels.getBasicPythonAlgorithm(),
                AlgorithmModels.getOptimizedJavaAlgorithm(),
                AlgorithmModels.getHashAlgorithmJava(),
                AlgorithmModels.getHashAlgorithmPython()
            )
        );
    }
    
    /**
     * 获取题目变形
     */
    @GetMapping("/variants")
    public Map<String, Object> getVariants() {
        return Map.of(
            "variants", VariantsModels.getAllVariants()
        );
    }
    
    /**
     * 获取单个变形详情
     */
    @GetMapping("/variants/{id}")
    public Map<String, Object> getVariant(@PathVariable String id) {
        return VariantsModels.getAllVariants().stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .map(v -> Map.of(
                "id", v.getId(),
                "name", v.getName(),
                "description", v.getDescription(),
                "example", v.getExample(),
                "solution", v.getSolution(),
                "keywords", v.getKeywords()
            ))
            .orElse(Map.of("error", "未找到该变形题目"));
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of(
            "status", "UP",
            "service", "Bulls and Cows API",
            "version", "1.0.0"
        );
    }
    
    /**
     * 查看游戏答案
     */
    @GetMapping("/game/{gameId}/answer")
    public Map<String, Object> getAnswer(@PathVariable String gameId) {
        String answer = gameService.getAnswer(gameId);
        if (answer == null) {
            return Map.of("error", "游戏不存在");
        }
        return Map.of(
            "gameId", gameId,
            "answer", answer
        );
    }
    
    /**
     * 验证谜题答案
     */
    @PostMapping("/puzzle/verify")
    public Map<String, Object> verifyAnswer(@RequestBody Map<String, String> request) {
        String puzzleId = request.get("puzzleId");
        String answer = request.get("answer");
        
        if (puzzleId == null || answer == null) {
            return Map.of("error", "参数不完整");
        }
        
        return gameService.verifyAnswer(puzzleId, answer);
    }
    
    /**
     * 生成推理谜题
     */
    @PostMapping("/puzzle/generate")
    public Map<String, Object> generatePuzzle(@RequestBody(required = false) Map<String, Boolean> request) {
        boolean allowDuplicates = request != null && Boolean.TRUE.equals(request.get("allowDuplicates"));
        return gameService.generatePuzzle(allowDuplicates);
    }

    /**
     * 保存练习记录
     */
    @PostMapping("/record/save")
    public Map<String, Object> saveRecord(@RequestBody Map<String, Object> request,
                                        Authentication authentication) {
        if (authentication == null) {
            return Map.of("error", "请先登录");
        }

        Long userId = getUserIdFromAuth(authentication);
        if (userId == null) {
            return Map.of("error", "无法获取用户信息");
        }

        try {
            String secretNumber = (String) request.get("secretNumber");
            Integer guessCount = (Integer) request.get("guessCount");
            Long timeSpentMs = request.get("timeSpentMs") != null
                    ? ((Number) request.get("timeSpentMs")).longValue() : 0L;
            Boolean allowDuplicates = (Boolean) request.get("allowDuplicates");
            String resultStr = (String) request.get("gameResult");
            PracticeRecord.GameResult gameResult = "WIN".equals(resultStr)
                    ? PracticeRecord.GameResult.WIN : PracticeRecord.GameResult.QUIT;

            PracticeRecord record = practiceRecordService.createRecord(
                    userId, secretNumber, guessCount, timeSpentMs, allowDuplicates, gameResult);

            // 如果赢了，更新排行榜
            if (gameResult == PracticeRecord.GameResult.WIN) {
                rankingService.updateRanking(userId, record);
            }

            return Map.of(
                    "success", true,
                    "recordId", record.getId(),
                    "message", "记录保存成功"
            );
        } catch (Exception e) {
            return Map.of("error", "保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的练习记录
     */
    @GetMapping("/record/list")
    public Map<String, Object> getUserRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        if (authentication == null) {
            return Map.of("error", "请先登录");
        }

        Long userId = getUserIdFromAuth(authentication);
        if (userId == null) {
            return Map.of("error", "无法获取用户信息");
        }

        Page<PracticeRecord> records = practiceRecordService.getUserRecords(userId, page, size);

        List<Map<String, Object>> recordList = records.getContent().stream()
                .map(r -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", r.getId());
                    item.put("secretNumber", r.getSecretNumber());
                    item.put("guessCount", r.getGuessCount());
                    item.put("timeSpentMs", r.getTimeSpentMs());
                    item.put("allowDuplicates", r.getAllowDuplicates());
                    item.put("gameResult", r.getGameResult().name());
                    item.put("createdAt", r.getCreatedAt());
                    return item;
                })
                .toList();

        return Map.of(
                "records", recordList,
                "totalPages", records.getTotalPages(),
                "totalElements", records.getTotalElements(),
                "currentPage", page
        );
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/record/stats")
    public Map<String, Object> getUserStatistics(Authentication authentication) {
        if (authentication == null) {
            return Map.of("error", "请先登录");
        }

        Long userId = getUserIdFromAuth(authentication);
        if (userId == null) {
            return Map.of("error", "无法获取用户信息");
        }

        return practiceRecordService.getUserStatistics(userId);
    }

    /**
     * 获取排行榜
     */
    @GetMapping("/ranking")
    public Map<String, Object> getRanking(
            @RequestParam(defaultValue = "GUESS_COUNT") String type,
            @RequestParam(defaultValue = "20") int limit) {
        Ranking.RankingType rankingType = Ranking.RankingType.valueOf(type);
        List<Map<String, Object>> rankings = rankingService.getRanking(rankingType, limit);
        return Map.of("rankings", rankings, "type", type);
    }

    /**
     * 获取用户在排行榜中的位置
     */
    @GetMapping("/ranking/me")
    public Map<String, Object> getMyRanking(
            @RequestParam(defaultValue = "GUESS_COUNT") String type,
            Authentication authentication) {
        if (authentication == null) {
            return Map.of("error", "请先登录");
        }

        Long userId = getUserIdFromAuth(authentication);
        if (userId == null) {
            return Map.of("error", "无法获取用户信息");
        }

        Ranking.RankingType rankingType = Ranking.RankingType.valueOf(type);
        Map<String, Object> ranking = rankingService.getUserRanking(userId, rankingType);

        if (ranking == null) {
            return Map.of("ranked", false, "message", "你还没有上榜，继续加油！");
        }

        ranking.put("ranked", true);
        return ranking;
    }

    /**
     * 从Authentication获取用户ID
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // 尝试从主体中获取用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
