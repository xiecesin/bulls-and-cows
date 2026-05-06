package com.bullscows.model;

import java.util.List;

/**
 * 猜测结果模型
 */
public class GuessResult {
    private String guess;
    private int bulls;
    private int cows;
    private int guessCount;
    private boolean success;
    private List<ReasoningStep> reasoning;

    public GuessResult() {}

    public GuessResult(String guess, int bulls, int cows, int guessCount, boolean success) {
        this.guess = guess;
        this.bulls = bulls;
        this.cows = cows;
        this.guessCount = guessCount;
        this.success = success;
    }

    // Getters and Setters
    public String getGuess() { return guess; }
    public void setGuess(String guess) { this.guess = guess; }
    public int getBulls() { return bulls; }
    public void setBulls(int bulls) { this.bulls = bulls; }
    public int getCows() { return cows; }
    public void setCows(int cows) { this.cows = cows; }
    public int getGuessCount() { return guessCount; }
    public void setGuessCount(int guessCount) { this.guessCount = guessCount; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<ReasoningStep> getReasoning() { return reasoning; }
    public void setReasoning(List<ReasoningStep> reasoning) { this.reasoning = reasoning; }

    /**
     * 获取格式化结果
     */
    public String getFormattedResult() {
        return bulls + "A" + cows + "B";
    }
}
