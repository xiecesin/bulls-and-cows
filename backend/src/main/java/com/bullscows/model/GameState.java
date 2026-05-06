package com.bullscows.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏状态模型
 */
public class GameState {
    private String gameId;
    private String secret;
    private int guessCount;
    private LocalDateTime startTime;
    private List<GuessResult> history;
    private boolean active;

    public GameState() {
        this.history = new ArrayList<>();
        this.guessCount = 0;
        this.active = true;
        this.startTime = LocalDateTime.now();
    }

    public GameState(String gameId, String secret) {
        this();
        this.gameId = gameId;
        this.secret = secret;
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public int getGuessCount() { return guessCount; }
    public void setGuessCount(int guessCount) { this.guessCount = guessCount; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public List<GuessResult> getHistory() { return history; }
    public void setHistory(List<GuessResult> history) { this.history = history; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void addGuess(GuessResult result) {
        this.history.add(result);
        this.guessCount++;
    }
}
