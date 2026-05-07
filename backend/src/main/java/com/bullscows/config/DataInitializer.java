package com.bullscows.config;

import com.bullscows.entity.User;
import com.bullscows.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
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
        
        // 默认账户（从 config.ini 读取或使用默认值）
        String adminUser = "admin";
        String adminPass = "admin123";
        String adminEmail = "admin@example.com";
        
        String testUser = "test";
        String testPass = "test123";
        String testEmail = "test@example.com";
        
        // 尝试从配置覆盖
        Map<String, String> admin = config.get("admin");
        if (admin != null) {
            adminUser = admin.getOrDefault("username", adminUser);
            adminPass = admin.getOrDefault("password", adminPass);
            adminEmail = admin.getOrDefault("email", adminEmail);
        }
        
        Map<String, String> test = config.get("test");
        if (test != null) {
            testUser = test.getOrDefault("username", testUser);
            testPass = test.getOrDefault("password", testPass);
            testEmail = test.getOrDefault("email", testEmail);
        }
        
        // 创建或更新管理员账户
        User adminUserEntity = userRepository.findByUsername(adminUser).orElse(null);
        if (adminUserEntity == null) {
            adminUserEntity = new User();
            adminUserEntity.setUsername(adminUser);
            adminUserEntity.setPassword(passwordEncoder.encode(adminPass));
            adminUserEntity.setEmail(adminEmail);
            adminUserEntity.setRole(User.Role.ADMIN);
            adminUserEntity.setStatus(User.Status.ACTIVE);
            userRepository.save(adminUserEntity);
            System.out.println("✓ 管理员账户已创建: " + adminUser + " / " + adminPass);
        } else if (adminUserEntity.getRole() != User.Role.ADMIN) {
            // 确保 admin 用户的角色是 ADMIN
            adminUserEntity.setRole(User.Role.ADMIN);
            adminUserEntity.setStatus(User.Status.ACTIVE);
            userRepository.save(adminUserEntity);
            System.out.println("✓ 管理员角色已更新: " + adminUser);
        }
        
        // 创建测试账户
        if (!userRepository.existsByUsername(testUser)) {
            User user = new User();
            user.setUsername(testUser);
            user.setPassword(passwordEncoder.encode(testPass));
            user.setEmail(testEmail);
            user.setRole(User.Role.USER);
            user.setStatus(User.Status.ACTIVE);
            userRepository.save(user);
            System.out.println("✓ 测试账户已创建: " + testUser + " / " + testPass);
        }
    }
    
    private Map<String, Map<String, String>> loadConfig() {
        Map<String, Map<String, String>> config = new HashMap<>();
        try {
            // 尝试多个路径
            String[] paths = {
                "config.ini",
                "./config.ini",
                "../config.ini",
                "backend/config.ini",
                System.getProperty("user.dir") + "/config.ini",
                System.getProperty("user.dir") + "/backend/config.ini"
            };
            
            File configFile = null;
            for (String path : paths) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    configFile = f;
                    System.out.println("找到配置文件: " + f.getAbsolutePath());
                    break;
                }
            }
            
            if (configFile == null) {
                System.out.println("未找到 config.ini，使用默认账户");
                return config;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
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
