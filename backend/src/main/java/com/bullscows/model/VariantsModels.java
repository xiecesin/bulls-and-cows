package com.bullscows.model;

import java.util.List;

/**
 * 题目变形模型
 */
public class VariantsModels {
    
    private String id;
    private String name;
    private String description;
    private String example;
    private String solution;
    private List<String> keywords;

    public VariantsModels() {}

    public VariantsModels(String id, String name, String description, String example, String solution, List<String> keywords) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.example = example;
        this.solution = solution;
        this.keywords = keywords;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    /**
     * 获取所有变形题目
     */
    public static List<VariantsModels> getAllVariants() {
        return List.of(
            getClassicVariant(),
            getNVariant(),
            getColorVariant(),
            getMultiplayerVariant(),
            getAiVariant(),
            getReverseVariant()
        );
    }

    /**
     * 经典4位数变形
     */
    public static VariantsModels getClassicVariant() {
        return new VariantsModels(
            "classic",
            "经典4位数字猜谜",
            "给定一个4位数字（可能包含重复数字），猜测另一个4位数字，返回Bulls和Cows的数量。",
            "secret = \"1123\", guess = \"0111\"\n结果: \"1A1B\"\n解释：第一个位置的1匹配（Bull），后三个数字中的1与剩余的1匹配（Cow）",
            "使用数组统计法，分别统计公牛位置和非公牛位置的数字出现次数。",
            List.of("经典", "4位数", "Bulls and Cows", "LeetCode 299")
        );
    }

    /**
     * N位数变形
     */
    public static VariantsModels getNVariant() {
        return new VariantsModels(
            "n-digit",
            "N位数通用版本",
            "将经典问题推广到任意位数，可以用于不同难度级别的练习。位数越多，搜索空间越大。",
            "secret = \"12345\", guess = \"54321\"\n结果: \"0A5B\"\n解释：所有数字都猜对了但位置都不对",
            "核心算法与4位版本相同，只是将固定长度改为可变长度参数。",
            List.of("N位", "通用", "扩展", "难度调节")
        );
    }

    /**
     * 颜色/字符版变形
     */
    public static VariantsModels getColorVariant() {
        return new VariantsModels(
            "color",
            "颜色序列版",
            "将数字替换为颜色或其他字符，例如红、黄、蓝、绿四种颜色的排列猜测。",
            "secret = \"RGBY\", guess = \"GRBY\"\n结果: \"2A2B\"\n解释：G和B位置正确（R在Y位，Y在R位）",
            "将颜色映射为字符，使用相同算法求解。",
            List.of("颜色", "字符", "映射", "可视化")
        );
    }

    /**
     * 多人协作变形
     */
    public static VariantsModels getMultiplayerVariant() {
        return new VariantsModels(
            "multiplayer",
            "多人协作猜数字",
            "多个玩家同时猜测，每个人只知道自己的猜测结果，不知道其他人的猜测。需要协作找出正确答案。",
            "玩家A猜测1234得到\"0A2B\"\n玩家B猜测5678得到\"1A0B\"\n协作推理找出正确答案",
            "需要收集多个玩家的反馈信息，通过逻辑推理和约束满足来缩小可能的答案范围。",
            List.of("多人", "协作", "社交", "约束满足")
        );
    }

    /**
     * AI智能猜测变形
     */
    public static VariantsModels getAiVariant() {
        return new VariantsModels(
            "ai-guess",
            "AI智能猜测算法",
            "让计算机扮演猜测者，通过优化策略在最少次数内猜出正确答案。经典的二分查找变体。",
            "使用Minimax策略或启发式搜索：\n第1次猜测: 1234 → 0A2B\n第2次猜测: 3456 → 1A1B\n...",
            "1. 候选集剪枝：根据反馈消除不可能的数字组合\n2. 信息熵最大化：选择能最大化信息增益的猜测\n3. 启发式搜索：使用评估函数引导搜索方向",
            List.of("AI", "智能", "Minimax", "启发式", "搜索算法")
        );
    }

    /**
     # 反向猜测变形
     */
    public static VariantsModels getReverseVariant() {
        return new VariantsModels(
            "reverse",
            "反向猜测（Mastermind）",
            "经典的Mastermind桌游变体，猜测者需要通过有限次数的猜测来确定密码。",
            "secret = [1, 2, 3, 4]\nguess = [1, 3, 5, 6]\n结果: 1个exact match, 1个partial match",
            "核心思想与Bulls and Cows相同，但通常有颜色数量限制和最大猜测次数限制。",
            List.of("Mastermind", "桌游", "反向", "限制次数")
        );
    }
}
