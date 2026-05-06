package com.bullscows.service;

import com.bullscows.model.GameState;
import com.bullscows.model.GuessResult;
import com.bullscows.model.ReasoningStep;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 猜数字游戏核心服务
 */
@Service
public class GameService {
    
    private final Map<String, GameState> games = new HashMap<>();
    
    /**
     * 开始新游戏
     */
    public GameState startNewGame() {
        String gameId = UUID.randomUUID().toString();
        String secret = generateSecret();
        GameState game = new GameState(gameId, secret);
        games.put(gameId, game);
        return game;
    }
    
    /**
     * 生成4位随机数字
     */
    private String generateSecret() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 获取游戏状态
     */
    public GameState getGame(String gameId) {
        return games.get(gameId);
    }
    
    /**
     * 处理猜测
     */
    public GuessResult processGuess(String gameId, String guess) {
        GameState game = games.get(gameId);
        if (game == null || !game.isActive()) {
            return null;
        }
        
        String secret = game.getSecret();
        GuessResult result = calculateWithReasoning(secret, guess);
        result.setGuessCount(game.getGuessCount() + 1);
        
        if (result.getBulls() == 4) {
            result.setSuccess(true);
            game.setActive(false);
        } else {
            result.setSuccess(false);
        }
        
        game.addGuess(result);
        return result;
    }
    
    /**
     * 计算结果（带推理过程）
     */
    public GuessResult calculateWithReasoning(String secret, String guess) {
        List<ReasoningStep> steps = new ArrayList<>();
        int bulls = 0;
        int cows = 0;
        
        // 步骤1：逐位比较，统计公牛
        steps.add(new ReasoningStep(1, "逐位比较，统计公牛位置", 
            String.format("比较 %s 和 %s 的每一位", secret, guess)));
        
        int[] count = new int[10];
        for (int i = 0; i < 4; i++) {
            if (secret.charAt(i) == guess.charAt(i)) {
                bulls++;
            } else {
                count[secret.charAt(i) - '0']++;
            }
        }
        
        steps.add(new ReasoningStep(2, "统计非公牛位置的数字", 
            String.format("秘密数字的非公牛位置: %s", 
                getNonBulls(secret, guess))));
        
        // 步骤3：统计奶牛
        steps.add(new ReasoningStep(3, "统计奶牛（数字对但位置错）", 
            "检查猜测中的非公牛数字是否能在秘密数字的非公牛位置中找到"));
        
        for (int i = 0; i < 4; i++) {
            if (secret.charAt(i) != guess.charAt(i) && 
                count[guess.charAt(i) - '0'] > 0) {
                cows++;
                count[guess.charAt(i) - '0']--;
            }
        }
        
        steps.add(new ReasoningStep(4, "最终结果", 
            String.format("公牛(Bulls): %d个\n奶牛(Cows): %d个\n最终答案: %dA%dB", 
                bulls, cows, bulls, cows)));
        
        GuessResult result = new GuessResult(guess, bulls, cows, 0, false);
        result.setReasoning(steps);
        return result;
    }
    
    /**
     * 获取非公牛位置的数字
     */
    private String getNonBulls(String secret, String guess) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (secret.charAt(i) != guess.charAt(i)) {
                sb.append(secret.charAt(i));
            }
        }
        return sb.length() > 0 ? sb.toString() : "无";
    }
    
    /**
     * 简单的结果计算（无推理）
     */
    public String calculateHint(String secret, String guess) {
        int bulls = 0;
        int cows = 0;
        int[] count = new int[10];
        
        for (int i = 0; i < secret.length(); i++) {
            if (secret.charAt(i) == guess.charAt(i)) {
                bulls++;
            } else {
                count[secret.charAt(i) - '0']++;
            }
        }
        
        for (int i = 0; i < secret.length(); i++) {
            if (secret.charAt(i) != guess.charAt(i) && 
                count[guess.charAt(i) - '0'] > 0) {
                cows++;
                count[guess.charAt(i) - '0']--;
            }
        }
        
        return bulls + "A" + cows + "B";
    }
    
    /**
     * 结束游戏
     */
    public void endGame(String gameId) {
        GameState game = games.get(gameId);
        if (game != null) {
            game.setActive(false);
        }
    }
    
    /**
     * 验证猜测格式
     */
    public boolean isValidGuess(String guess) {
        if (guess == null || guess.length() != 4) {
            return false;
        }
        return guess.chars().allMatch(Character::isDigit);
    }
    
    /**
     * 开始新游戏（可配置是否允许重复）
     */
    public GameState startNewGame(boolean allowDuplicates) {
        String gameId = UUID.randomUUID().toString();
        String secret = generateSecret(allowDuplicates);
        GameState game = new GameState(gameId, secret);
        games.put(gameId, game);
        return game;
    }
    
    /**
     * 生成4位随机数字（可配置是否允许重复）
     */
    private String generateSecret(boolean allowDuplicates) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        if (allowDuplicates) {
            for (int i = 0; i < 4; i++) {
                sb.append(random.nextInt(10));
            }
        } else {
            List<Integer> digits = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            Collections.shuffle(digits, random);
            for (int i = 0; i < 4; i++) {
                sb.append(digits.get(i));
            }
        }
        return sb.toString();
    }
    
    /**
     * 获取游戏答案
     */
    public String getAnswer(String gameId) {
        GameState game = games.get(gameId);
        return game != null ? game.getSecret() : null;
    }
    
    /**
     * 验证用户提交的答案
     */
    public Map<String, Object> verifyAnswer(String gameId, String answer) {
        GameState game = games.get(gameId);
        if (game == null) {
            return Map.of("error", true, "message", "游戏不存在");
        }
        
        if (!isValidGuess(answer)) {
            return Map.of("error", true, "message", "请输入4位数字");
        }
        
        boolean correct = answer.equals(game.getSecret());
        return Map.of(
            "correct", correct,
            "yourAnswer", answer,
            "secret", correct ? answer : game.getSecret(),
            "message", correct ? "回答正确！" : "回答错误，正确答案是 " + game.getSecret()
        );
    }
    
    /**
     * 生成谜题：给定答案，生成若干条合理的猜测记录
     */
    public Map<String, Object> generatePuzzle(boolean allowDuplicates) {
        Random random = new Random();
        String secret = generateSecret(allowDuplicates);
        List<String> allGuesses = generateAllGuesses(secret, allowDuplicates);
        
        // 生成 3-5 条合理的猜测记录
        int numGuesses = 3 + random.nextInt(3);
        numGuesses = Math.min(numGuesses, allGuesses.size());
        
        List<Map<String, Object>> history = new ArrayList<>();
        Set<String> usedGuesses = new HashSet<>();
        
        // 选择能提供最多信息的猜测
        List<String> candidates = allGuesses.stream()
            .filter(g -> !g.equals(secret))
            .sorted((a, b) -> {
                // 优先选择信息量大的（不是 0A0B 的）
                String hintA = calculateHint(secret, a);
                String hintB = calculateHint(secret, b);
                return hintB.compareTo(hintA);
            })
            .toList();
        
        for (int i = 0; i < numGuesses && i < candidates.size(); i++) {
            String guess = candidates.get(i);
            if (usedGuesses.contains(guess)) continue;
            
            String hint = calculateHint(secret, guess);
            // 只添加有信息量的猜测（不是 0A0B）
            if (!"0A0B".equals(hint)) {
                history.add(Map.of(
                    "guess", guess,
                    "bulls", hint.charAt(0) - '0',
                    "cows", hint.charAt(2) - '0',
                    "hint", hint
                ));
                usedGuesses.add(guess);
            }
        }
        
        // 如果还不够，随机补充
        for (String guess : candidates) {
            if (history.size() >= numGuesses) break;
            if (!usedGuesses.contains(guess)) {
                String hint = calculateHint(secret, guess);
                history.add(Map.of(
                    "guess", guess,
                    "bulls", hint.charAt(0) - '0',
                    "cows", hint.charAt(2) - '0',
                    "hint", hint
                ));
                usedGuesses.add(guess);
            }
        }
        
        // 打乱顺序
        Collections.shuffle(history, random);
        
        String puzzleId = UUID.randomUUID().toString();
        games.put(puzzleId, new GameState(puzzleId, secret));
        
        return Map.of(
            "puzzleId", puzzleId,
            "history", history,
            "allowDuplicates", allowDuplicates,
            "hint", "根据以下记录，推断出秘密数字"
        );
    }
    
    /**
     * 生成所有可能的有效猜测（基于某种策略减少候选）
     */
    private List<String> generateAllGuesses(String secret, boolean allowDuplicates) {
        List<String> guesses = new ArrayList<>();
        Random random = new Random();
        
        // 生成一些随机猜测用于构建谜题
        Set<String> used = new HashSet<>();
        used.add(secret);
        
        for (int i = 0; i < 50; i++) {
            String guess;
            if (allowDuplicates) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    sb.append(random.nextInt(10));
                }
                guess = sb.toString();
            } else {
                List<Integer> digits = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
                Collections.shuffle(digits, random);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 4; j++) {
                    sb.append(digits.get(j));
                }
                guess = sb.toString();
            }
            if (!used.contains(guess)) {
                used.add(guess);
                guesses.add(guess);
            }
        }
        
        return guesses;
    }
}
