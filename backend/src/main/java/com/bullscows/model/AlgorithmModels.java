package com.bullscows.model;

/**
 * 算法模型
 */
public class AlgorithmModels {
    
    private String name;
    private String language;
    private String code;
    private String description;
    private String complexity;

    public AlgorithmModels() {}

    public AlgorithmModels(String name, String language, String code, String description, String complexity) {
        this.name = name;
        this.language = language;
        this.code = code;
        this.description = description;
        this.complexity = complexity;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getComplexity() { return complexity; }
    public void setComplexity(String complexity) { this.complexity = complexity; }

    /**
     * 基础算法 - Java 实现
     */
    public static AlgorithmModels getBasicJavaAlgorithm() {
        String code = 
            "/*\n" +
            " * 基础算法 - Java 实现\n" +
            " * 时间复杂度: O(n)\n" +
            " * 空间复杂度: O(1)\n" +
            " */\n" +
            "public class BullsAndCows {\n" +
            "    \n" +
            "    public String getHint(String secret, String guess) {\n" +
            "        int bulls = 0;\n" +
            "        int cows = 0;\n" +
            "        int[] count = new int[10];\n" +
            "        \n" +
            "        // 第一遍：统计公牛和秘密数字中非公牛的数字\n" +
            "        for (int i = 0; i < secret.length(); i++) {\n" +
            "            if (secret.charAt(i) == guess.charAt(i)) {\n" +
            "                bulls++;\n" +
            "            } else {\n" +
            "                count[secret.charAt(i) - '0']++;\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        // 第二遍：统计奶牛\n" +
            "        for (int i = 0; i < secret.length(); i++) {\n" +
            "            if (secret.charAt(i) != guess.charAt(i) && \n" +
            "                count[guess.charAt(i) - '0'] > 0) {\n" +
            "                cows++;\n" +
            "                count[guess.charAt(i) - '0']--;\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        return bulls + \"A\" + cows + \"B\";\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        BullsAndCows game = new BullsAndCows();\n" +
            "        System.out.println(game.getHint(\"1123\", \"0111\"));  // 输出: 1A1B\n" +
            "    }\n" +
            "}";
        return new AlgorithmModels(
            "基础算法（双遍扫描）",
            "Java",
            code,
            "使用数组统计数字出现次数，两遍扫描完成计算。公牛直接比较位置，奶牛通过计数间接匹配。",
            "时间: O(n), 空间: O(1)"
        );
    }

    /**
     * 基础算法 - Python 实现
     */
    public static AlgorithmModels getBasicPythonAlgorithm() {
        String code = 
            "# 基础算法 - Python 实现\n" +
            "# 时间复杂度: O(n)\n" +
            "# 空间复杂度: O(1)\n" +
            "\n" +
            "def get_hint(secret: str, guess: str) -> str:\n" +
            "    '''\n" +
            "    计算猜测结果提示\n" +
            "    \n" +
            "    Args:\n" +
            "        secret: 秘密数字\n" +
            "        guess: 猜测数字\n" +
            "    \n" +
            "    Returns:\n" +
            "        格式化的提示字符串，如 1A1B\n" +
            "    '''\n" +
            "    bulls = 0\n" +
            "    cows = 0\n" +
            "    count = [0] * 10  # 统计0-9数字出现次数\n" +
            "    \n" +
            "    # 第一遍：统计公牛和秘密数字中非公牛的数字\n" +
            "    for i in range(len(secret)):\n" +
            "        if secret[i] == guess[i]:\n" +
            "            bulls += 1\n" +
            "        else:\n" +
            "            count[int(secret[i])] += 1\n" +
            "    \n" +
            "    # 第二遍：统计奶牛\n" +
            "    for i in range(len(secret)):\n" +
            "        if secret[i] != guess[i] and count[int(guess[i])] > 0:\n" +
            "            cows += 1\n" +
            "            count[int(guess[i])] -= 1\n" +
            "    \n" +
            "    return f\"{bulls}A{cows}B\"\n" +
            "\n" +
            "# 测试\n" +
            "if __name__ == \"__main__\":\n" +
            "    print(get_hint(\"1123\", \"0111\"))  # 输出: 1A1B";
        return new AlgorithmModels(
            "基础算法（双遍扫描）",
            "Python",
            code,
            "使用列表统计数字出现次数，两遍扫描完成计算。Python实现简洁直观。",
            "时间: O(n), 空间: O(1)"
        );
    }

    /**
     * 优化算法 - Java 实现（单遍扫描）
     */
    public static AlgorithmModels getOptimizedJavaAlgorithm() {
        String code = 
            "/*\n" +
            " * 优化算法 - Java 实现（单遍扫描）\n" +
            " * 时间复杂度: O(n)\n" +
            " * 空间复杂度: O(1)\n" +
            " */\n" +
            "public class BullsAndCowsOptimized {\n" +
            "    \n" +
            "    public String getHint(String secret, String guess) {\n" +
            "        int bulls = 0;\n" +
            "        int cows = 0;\n" +
            "        int[] count = new int[10];\n" +
            "        \n" +
            "        for (int i = 0; i < secret.length(); i++) {\n" +
            "            char s = secret.charAt(i);\n" +
            "            char g = guess.charAt(i);\n" +
            "            \n" +
            "            if (s == g) {\n" +
            "                bulls++;\n" +
            "            } else {\n" +
            "                // 使用两个方向计数来处理边界情况\n" +
            "                // count[s] > 0 表示秘密数字中有未匹配的该数字\n" +
            "                // count[g] < 0 表示猜测数字中有未匹配的该数字\n" +
            "                if (count[s] > 0) {\n" +
            "                    cows++;\n" +
            "                    count[s]--;\n" +
            "                } else {\n" +
            "                    count[s] = -1;\n" +
            "                }\n" +
            "                \n" +
            "                if (count[g] < 0) {\n" +
            "                    cows++;\n" +
            "                    count[g]++;\n" +
            "                } else {\n" +
            "                    count[g] = 1;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        return bulls + \"A\" + cows + \"B\";\n" +
            "    }\n" +
            "}";
        return new AlgorithmModels(
            "优化算法（单遍扫描）",
            "Java",
            code,
            "通过巧妙的正负计数，在单遍扫描中同时处理公牛和奶牛的统计。",
            "时间: O(n), 空间: O(1)"
        );
    }

    /**
     * 哈希表算法 - Java 实现
     */
    public static AlgorithmModels getHashAlgorithmJava() {
        String code = 
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "/*\n" +
            " * 使用哈希表的算法 - Java 实现\n" +
            " * 时间复杂度: O(n)\n" +
            " * 空间复杂度: O(k)，k为数字种类数\n" +
            " */\n" +
            "public class BullsAndCowsHash {\n" +
            "    \n" +
            "    public String getHint(String secret, String guess) {\n" +
            "        int bulls = 0;\n" +
            "        Map<Character, Integer> secretCount = new HashMap<>();\n" +
            "        Map<Character, Integer> guessCount = new HashMap<>();\n" +
            "        \n" +
            "        for (int i = 0; i < secret.length(); i++) {\n" +
            "            char s = secret.charAt(i);\n" +
            "            char g = guess.charAt(i);\n" +
            "            \n" +
            "            if (s == g) {\n" +
            "                bulls++;\n" +
            "            } else {\n" +
            "                secretCount.put(s, secretCount.getOrDefault(s, 0) + 1);\n" +
            "                guessCount.put(g, guessCount.getOrDefault(g, 0) + 1);\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        int cows = 0;\n" +
            "        for (char digit : guessCount.keySet()) {\n" +
            "            if (secretCount.containsKey(digit)) {\n" +
            "                cows += Math.min(secretCount.get(digit), guessCount.get(digit));\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        return bulls + \"A\" + cows + \"B\";\n" +
            "    }\n" +
            "}";
        return new AlgorithmModels(
            "哈希表算法",
            "Java",
            code,
            "使用HashMap分别统计秘密数字和猜测数字中非公牛部分的出现次数，然后取最小值得到奶牛数量。",
            "时间: O(n), 空间: O(k)"
        );
    }
    
    /**
     * 哈希表算法 - Python 实现
     */
    public static AlgorithmModels getHashAlgorithmPython() {
        String code = 
            "# 使用字典的算法 - Python 实现\n" +
            "# 时间复杂度: O(n)\n" +
            "# 空间复杂度: O(k)，k为数字种类数\n" +
            "\n" +
            "def get_hint(secret: str, guess: str) -> str:\n" +
            "    '''\n" +
            "    使用字典（哈希表）计算猜测结果提示\n" +
            "    \n" +
            "    Args:\n" +
            "        secret: 秘密数字\n" +
            "        guess: 猜测数字\n" +
            "    \n" +
            "    Returns:\n" +
            "        格式化的提示字符串，如 1A1B\n" +
            "    '''\n" +
            "    bulls = 0\n" +
            "    secret_count = {}\n" +
            "    guess_count = {}\n" +
            "    \n" +
            "    for i in range(len(secret)):\n" +
            "        s, g = secret[i], guess[i]\n" +
            "        if s == g:\n" +
            "            bulls += 1\n" +
            "        else:\n" +
            "            secret_count[s] = secret_count.get(s, 0) + 1\n" +
            "            guess_count[g] = guess_count.get(g, 0) + 1\n" +
            "    \n" +
            "    # 统计奶牛数量\n" +
            "    cows = 0\n" +
            "    for digit in guess_count:\n" +
            "        if digit in secret_count:\n" +
            "            cows += min(secret_count[digit], guess_count[digit])\n" +
            "    \n" +
            "    return f\"{bulls}A{cows}B\"\n" +
            "\n" +
            "# 测试\n" +
            "if __name__ == \"__main__\":\n" +
            "    print(get_hint(\"1123\", \"0111\"))  # 输出: 1A1B\"\n";
        return new AlgorithmModels(
            "哈希表算法",
            "Python",
            code,
            "使用dict（字典/哈希表）分别统计秘密数字和猜测数字中非公牛部分的出现次数，然后取最小值得到奶牛数量。Python的dict实现简洁高效。",
            "时间: O(n), 空间: O(k)"
        );
    }
}
