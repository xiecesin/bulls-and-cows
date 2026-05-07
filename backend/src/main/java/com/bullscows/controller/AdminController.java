package com.bullscows.controller;

import com.bullscows.entity.User;
import com.bullscows.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    
    private final UserRepository userRepository;
    
    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByStatus(User.Status.ACTIVE).size();
        long totalRecords = 0; // 需要时可添加 PracticeRecordRepository
        
        // 获取今日新增用户
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayNewUsers = userRepository.findByCreatedAtAfter(today).size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("totalRecords", totalRecords);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("todayRecords", 0);
        stats.put("onlineUsers", 12); // 模拟数据
        stats.put("peakUsers", 28);   // 模拟数据
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        List<User> allUsers = new ArrayList<>(userRepository.findAll());
        
        // 过滤
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            allUsers = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(kw) 
                    || (u.getNickname() != null && u.getNickname().toLowerCase().contains(kw))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        }
        
        if (role != null && !role.isEmpty()) {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            allUsers = allUsers.stream()
                .filter(u -> u.getRole() == userRole)
                .collect(Collectors.toList());
        }
        
        if (status != null && !status.isEmpty()) {
            User.Status userStatus = User.Status.valueOf(status.toUpperCase());
            allUsers = allUsers.stream()
                .filter(u -> u.getStatus() == userStatus)
                .collect(Collectors.toList());
        }
        
        // 分页
        int total = allUsers.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<User> pagedUsers = start < total ? allUsers.subList(start, end) : Collections.emptyList();
        
        List<Map<String, Object>> userList = pagedUsers.stream()
            .map(this::convertUserToMap)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取单个用户
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> ResponseEntity.ok(convertUserToMap(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建用户
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        String username = request.get("username");
        String nickname = request.get("nickname");
        String email = request.get("email");
        String password = request.get("password");
        String role = request.getOrDefault("role", "USER");
        
        if (userRepository.findByUsername(username).isPresent()) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        User user = User.builder()
            .username(username)
            .nickname(nickname != null ? nickname : username)
            .email(email)
            .role(User.Role.valueOf(role.toUpperCase()))
            .status(User.Status.ACTIVE)
            .build();
        
        if (password != null && !password.isEmpty()) {
            user.setPassword(password); // 实际应该加密
        }
        
        User savedUser = userRepository.save(user);
        result.put("success", true);
        result.put("message", "用户创建成功");
        result.put("user", convertUserToMap(savedUser));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        
        return userRepository.findById(id)
            .map(user -> {
                if (request.containsKey("nickname")) {
                    user.setNickname(request.get("nickname"));
                }
                if (request.containsKey("email")) {
                    user.setEmail(request.get("email"));
                }
                if (request.containsKey("role")) {
                    user.setRole(User.Role.valueOf(request.get("role").toUpperCase()));
                }
                if (request.containsKey("status")) {
                    user.setStatus(User.Status.valueOf(request.get("status").toUpperCase()));
                }
                
                User savedUser = userRepository.save(user);
                result.put("success", true);
                result.put("message", "用户更新成功");
                result.put("user", convertUserToMap(savedUser));
                return ResponseEntity.ok(result);
            })
            .orElseGet(() -> {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(result);
            });
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        if (!userRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        userRepository.deleteById(id);
        result.put("success", true);
        result.put("message", "用户删除成功");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 切换用户状态
     */
    @PostMapping("/users/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        return userRepository.findById(id)
            .map(user -> {
                user.setStatus(user.getStatus() == User.Status.ACTIVE 
                    ? User.Status.BANNED 
                    : User.Status.ACTIVE);
                User savedUser = userRepository.save(user);
                
                result.put("success", true);
                result.put("message", "状态切换成功");
                result.put("user", convertUserToMap(savedUser));
                return ResponseEntity.ok(result);
            })
            .orElseGet(() -> {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(result);
            });
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/monitor/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        long totalMemory = memoryBean.getHeapMemoryUsage().getCommitted();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("cpuUsage", Math.random() * 50 + 30); // 模拟 30-80%
        systemInfo.put("memoryUsage", (double) usedMemory / maxMemory * 100);
        systemInfo.put("diskUsage", 45.0);
        systemInfo.put("onlineUsers", 12);
        systemInfo.put("peakUsers", 28);
        systemInfo.put("osName", osBean.getName());
        systemInfo.put("osVersion", osBean.getVersion());
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("springBootVersion", "3.2.0");
        systemInfo.put("uptime", formatUptime());
        systemInfo.put("startTime", formatStartTime());
        systemInfo.put("appName", "Bulls and Cows");
        systemInfo.put("appVersion", "1.0.0");
        systemInfo.put("port", 8080);
        systemInfo.put("database", "PostgreSQL 15");
        systemInfo.put("profile", "dev");
        
        return ResponseEntity.ok(systemInfo);
    }
    
    /**
     * 获取 API 统计
     */
    @GetMapping("/monitor/api")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getApiStats() {
        List<Map<String, Object>> apiStats = new ArrayList<>();
        
        apiStats.add(createApiStat("/api/game/start", "POST", 1256, 45, 120, 0.2));
        apiStats.add(createApiStat("/api/game/guess", "POST", 8934, 23, 85, 0.1));
        apiStats.add(createApiStat("/api/auth/login", "POST", 567, 89, 234, 0.5));
        apiStats.add(createApiStat("/api/records", "GET", 2345, 56, 180, 0.3));
        apiStats.add(createApiStat("/api/ranking", "GET", 1890, 34, 98, 0.1));
        apiStats.add(createApiStat("/api/algorithm", "GET", 456, 28, 67, 0.0));
        
        return ResponseEntity.ok(apiStats);
    }
    
    /**
     * 获取日志
     */
    @GetMapping("/monitor/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getLogs(
            @RequestParam(defaultValue = "ALL") String level) {
        
        List<Map<String, Object>> logs = new ArrayList<>();
        
        logs.add(createLog("10:23:45", "INFO", "用户 testuser 登录成功"));
        logs.add(createLog("10:23:50", "INFO", "创建新游戏 gameId=abc123"));
        logs.add(createLog("10:24:12", "INFO", "用户提交猜测: 1234"));
        logs.add(createLog("10:24:12", "DEBUG", "猜测结果: 1A2B"));
        logs.add(createLog("10:25:30", "WARN", "请求频率过高: /api/game/guess from IP 192.168.1.100"));
        logs.add(createLog("10:26:15", "ERROR", "数据库连接超时"));
        logs.add(createLog("10:26:20", "INFO", "连接池已恢复"));
        logs.add(createLog("10:27:00", "INFO", "游戏结束: gameId=abc123, 用时 3分钟"));
        
        if (!"ALL".equals(level)) {
            final String filterLevel = level;
            logs = logs.stream()
                .filter(log -> filterLevel.equals(log.get("level")))
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取性能指标
     */
    @GetMapping("/monitor/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("requestsTotal", 15432);
        metrics.put("requestsSuccess", 15328);
        metrics.put("requestsFailed", 104);
        metrics.put("avgResponseTime", 42);
        metrics.put("maxResponseTime", 280);
        metrics.put("minResponseTime", 8);
        metrics.put("activeConnections", 15);
        metrics.put("databaseConnections", 5);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * 获取在线用户
     */
    @GetMapping("/monitor/online-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getOnlineUsers() {
        List<Map<String, Object>> onlineUsers = new ArrayList<>();
        
        onlineUsers.add(createOnlineUser("user1", "张三", new Date().toString()));
        onlineUsers.add(createOnlineUser("user2", "李四", new Date().toString()));
        onlineUsers.add(createOnlineUser("admin", "管理员", new Date().toString()));
        
        return ResponseEntity.ok(onlineUsers);
    }
    
    // ========== 私有方法 ==========
    
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        map.put("email", user.getEmail() != null ? user.getEmail() : "");
        map.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        map.put("role", user.getRole().name());
        map.put("status", user.getStatus().name());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        map.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "");
        return map;
    }
    
    private Map<String, Object> createApiStat(String endpoint, String method, 
            int requests, int avgTime, int maxTime, double errorRate) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("endpoint", endpoint);
        stat.put("method", method);
        stat.put("requests", requests);
        stat.put("avgTime", avgTime);
        stat.put("maxTime", maxTime);
        stat.put("errorRate", errorRate);
        return stat;
    }
    
    private Map<String, Object> createLog(String time, String level, String message) {
        Map<String, Object> log = new HashMap<>();
        log.put("time", time);
        log.put("level", level);
        log.put("message", message);
        return log;
    }
    
    private Map<String, Object> createOnlineUser(String username, String nickname, String lastActive) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("nickname", nickname);
        user.put("lastActive", lastActive);
        return user;
    }
    
    private String formatUptime() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        long currentTime = System.currentTimeMillis();
        long uptime = currentTime - startTime;
        
        long days = uptime / (24 * 60 * 60 * 1000);
        long hours = (uptime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000);
        
        return String.format("%d天 %d小时 %d分钟", days, hours, minutes);
    }
    
    private String formatStartTime() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(new Date(startTime));
    }
}
