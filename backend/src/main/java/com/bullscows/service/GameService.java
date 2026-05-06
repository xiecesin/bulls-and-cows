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
}
