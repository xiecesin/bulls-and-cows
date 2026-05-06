package com.bullscows.model;

/**
 * 推理步骤模型
 */
public class ReasoningStep {
    private int step;
    private String description;
    private String detail;

    public ReasoningStep() {}

    public ReasoningStep(int step, String description, String detail) {
        this.step = step;
        this.description = description;
        this.detail = detail;
    }

    // Getters and Setters
    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
