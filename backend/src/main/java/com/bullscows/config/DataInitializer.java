package com.bullscows.config;

import com.bullscows.entity.User;
import com.bullscows.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化默认用户数据（从 config.ini 读取）
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        Map<String, Map<String, String>> config = loadConfig();
        
        // 创建管理员账户
        Map<String, String> admin = config.get("admin");
        if (admin != null && !userRepository.existsByUsername(admin.get("username"))) {
            User user = new User();
            user.setUsername(admin.get("username"));
            user.setPassword(passwordEncoder.encode(admin.get("password")));
            user.setEmail(admin.get("email"));
            user.setRole(User.Role.ADMIN);
            user.setStatus(User.Status.ACTIVE);
            userRepository.save(user);
            System.out.println("✓ 管理员账户已创建: " + admin.get("username") + " / " + admin.get("password"));
        }
        
        // 创建测试账户
        Map<String, String> test = config.get("test");
        if (test != null && !userRepository.existsByUsername(test.get("username"))) {
            User user = new User();
            user.setUsername(test.get("username"));
            user.setPassword(passwordEncoder.encode(test.get("password")));
            user.setEmail(test.get("email"));
            user.setRole(User.Role.USER);
            user.setStatus(User.Status.ACTIVE);
            userRepository.save(user);
            System.out.println("✓ 测试账户已创建: " + test.get("username") + " / " + test.get("password"));
        }
    }
    
    private Map<String, Map<String, String>> loadConfig() {
        Map<String, Map<String, String>> config = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("config.ini"));
            String line;
            Map<String, String> currentSection = null;
            String currentSectionName = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSectionName = line.substring(1, line.length() - 1);
                    currentSection = new HashMap<>();
                    config.put(currentSectionName, currentSection);
                } else if (line.contains("=") && currentSection != null) {
                    String[] parts = line.split("=", 2);
                    currentSection.put(parts[0].trim(), parts[1].trim());
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("配置文件加载失败，使用默认值");
        }
        return config;
    }
}
