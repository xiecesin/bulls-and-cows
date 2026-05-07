package com.bullscows.controller;

import com.bullscows.config.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 多人协作控制器
 */
@RestController
@RequestMapping("/api/collaboration")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollaborationController {
    
    // 模拟房间存储（生产环境应使用数据库）
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final AtomicInteger roomCounter = new AtomicInteger(1000);
    
    /**
     * 获取可用房间列表
     */
    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getRooms() {
        List<Map<String, Object>> roomList = rooms.values().stream()
            .filter(room -> !room.isGameStarted())
            .map(this::convertRoomToMap)
            .sorted((a, b) -> ((Date) b.get("createdAt")).compareTo((Date) a.get("createdAt")))
            .toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", roomList);
        result.put("total", roomList.size());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 创建房间
     */
    @PostMapping("/rooms")
    public ResponseEntity<Map<String, Object>> createRoom(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        
        String nickname = request.getOrDefault("nickname", principal.username());
        
        String roomCode = generateRoomCode();
        Room room = new Room(roomCode, principal.username(), nickname);
        rooms.put(roomCode, room);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "房间创建成功");
        result.put("room", convertRoomToMap(room));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取房间信息
     */
    @GetMapping("/rooms/{roomCode}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("room", convertRoomToMap(room));
        return ResponseEntity.ok(result);
    }
    
    /**
     * 加入房间
     */
    @PostMapping("/rooms/join")
    public ResponseEntity<Map<String, Object>> joinRoom(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        String roomCode = request.get("roomCode");
        String nickname = request.getOrDefault("nickname", principal.username());
        
        Room room = rooms.get(roomCode);
        if (room == null) {
            result.put("success", false);
            result.put("message", "房间不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (room.isGameStarted()) {
            result.put("success", false);
            result.put("message", "游戏已开始，无法加入");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (room.getPlayers().size() >= 2) {
            result.put("success", false);
            result.put("message", "房间已满");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (room.hasPlayer(principal.username())) {
            result.put("success", false);
            result.put("message", "您已经在房间中");
            return ResponseEntity.badRequest().body(result);
        }
        
        room.addPlayer(principal.username(), nickname);
        
        result.put("success", true);
        result.put("message", "加入成功");
        result.put("room", convertRoomToMap(room));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 离开房间
     */
    @DeleteMapping("/rooms/{roomCode}/leave")
    public ResponseEntity<Map<String, Object>> leaveRoom(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @PathVariable String roomCode) {
        
        Map<String, Object> result = new HashMap<>();
        Room room = rooms.get(roomCode);
        
        if (room == null) {
            result.put("success", false);
            result.put("message", "房间不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        room.removePlayer(principal.username());
        
        // 如果房间空了，删除房间
        if (room.getPlayers().isEmpty()) {
            rooms.remove(roomCode);
        }
        
        result.put("success", true);
        result.put("message", "已离开房间");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 开始游戏
     */
    @PostMapping("/rooms/{roomCode}/start")
    public ResponseEntity<Map<String, Object>> startGame(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @PathVariable String roomCode) {
        
        Map<String, Object> result = new HashMap<>();
        Room room = rooms.get(roomCode);
        
        if (room == null) {
            result.put("success", false);
            result.put("message", "房间不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (!room.getHost().equals(principal.username())) {
            result.put("success", false);
            result.put("message", "只有房主可以开始游戏");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (room.getPlayers().size() < 2) {
            result.put("success", false);
            result.put("message", "需要至少2名玩家才能开始游戏");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 生成答案（4个不重复数字）
        room.generateAnswer();
        room.setGameStarted(true);
        
        result.put("success", true);
        result.put("message", "游戏开始");
        result.put("room", convertRoomToMap(room));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 提交猜测
     */
    @PostMapping("/rooms/{roomCode}/guess")
    public ResponseEntity<Map<String, Object>> makeGuess(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @PathVariable String roomCode,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        Room room = rooms.get(roomCode);
        
        if (room == null) {
            result.put("success", false);
            result.put("message", "房间不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (!room.isGameStarted()) {
            result.put("success", false);
            result.put("message", "游戏尚未开始");
            return ResponseEntity.badRequest().body(result);
        }
        
        String guess = request.get("guess");
        if (guess == null || guess.length() != 4) {
            result.put("success", false);
            result.put("message", "猜测必须是4位数字");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 计算结果
        int bulls = 0, cows = 0;
        String answer = room.getAnswer();
        
        for (int i = 0; i < 4; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                bulls++;
            } else if (answer.indexOf(guess.charAt(i)) != -1) {
                cows++;
            }
        }
        
        // 记录猜测
        GuessRecord record = new GuessRecord(principal.username(), guess, bulls, cows);
        room.addGuessRecord(record);
        
        result.put("success", true);
        result.put("guess", guess);
        result.put("bulls", bulls);
        result.put("cows", cows);
        result.put("isWin", bulls == 4);
        result.put("guessCount", room.getGuessCount(principal.username()));
        
        // 如果猜中，更新游戏状态
        if (bulls == 4) {
            room.setWinner(principal.username());
            room.setGameStarted(false);
            result.put("message", "恭喜 " + room.getPlayerNickname(principal.username()) + " 猜中了答案！");
            result.put("winner", principal.username());
            result.put("answer", answer);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重置游戏
     */
    @PostMapping("/rooms/{roomCode}/reset")
    public ResponseEntity<Map<String, Object>> resetGame(
            @AuthenticationPrincipal JwtAuthFilter.UserPrincipal principal,
            @PathVariable String roomCode) {
        
        Map<String, Object> result = new HashMap<>();
        Room room = rooms.get(roomCode);
        
        if (room == null) {
            result.put("success", false);
            result.put("message", "房间不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        if (!room.getHost().equals(principal.username())) {
            result.put("success", false);
            result.put("message", "只有房主可以重置游戏");
            return ResponseEntity.badRequest().body(result);
        }
        
        room.reset();
        
        result.put("success", true);
        result.put("message", "游戏已重置，等待开始");
        result.put("room", convertRoomToMap(room));
        
        return ResponseEntity.ok(result);
    }
    
    // ========== 私有方法 ==========
    
    private String generateRoomCode() {
        return String.format("%04d", roomCounter.incrementAndGet());
    }
    
    private Map<String, Object> convertRoomToMap(Room room) {
        Map<String, Object> map = new HashMap<>();
        map.put("roomCode", room.getRoomCode());
        map.put("host", room.getHost());
        map.put("hostNickname", room.getHostNickname());
        map.put("players", room.getPlayers().entrySet().stream()
            .map(e -> {
                Map<String, Object> player = new HashMap<>();
                player.put("username", e.getKey());
                player.put("nickname", e.getValue());
                player.put("isHost", e.getKey().equals(room.getHost()));
                player.put("guessCount", room.getGuessCount(e.getKey()));
                return player;
            })
            .toList());
        map.put("playerCount", room.getPlayers().size());
        map.put("gameStarted", room.isGameStarted());
        map.put("answer", room.isGameStarted() ? null : room.getAnswer()); // 游戏未开始时显示答案预览
        map.put("winner", room.getWinner());
        map.put("createdAt", room.getCreatedAt());
        map.put("guessRecords", room.getGuessRecords().stream()
            .map(r -> {
                Map<String, Object> record = new HashMap<>();
                record.put("player", r.getPlayer());
                record.put("guess", r.getGuess());
                record.put("bulls", r.getBulls());
                record.put("cows", r.getCows());
                record.put("timestamp", r.getTimestamp());
                return record;
            })
            .toList());
        return map;
    }
    
    // ========== 内部类 ==========
    
    private static class Room {
        private final String roomCode;
        private final String host;
        private final String hostNickname;
        private final Map<String, String> players; // username -> nickname
        private final Map<String, Integer> guessCounts;
        private final List<GuessRecord> guessRecords;
        private String answer;
        private boolean gameStarted;
        private String winner;
        private final Date createdAt;
        
        public Room(String roomCode, String host, String hostNickname) {
            this.roomCode = roomCode;
            this.host = host;
            this.hostNickname = hostNickname;
            this.players = new HashMap<>();
            this.guessCounts = new HashMap<>();
            this.guessRecords = new ArrayList<>();
            this.gameStarted = false;
            this.winner = null;
            this.createdAt = new Date();
            
            // 房主自动加入
            players.put(host, hostNickname);
            guessCounts.put(host, 0);
        }
        
        public String getRoomCode() { return roomCode; }
        public String getHost() { return host; }
        public String getHostNickname() { return hostNickname; }
        public Map<String, String> getPlayers() { return players; }
        public String getAnswer() { return answer; }
        public boolean isGameStarted() { return gameStarted; }
        public String getWinner() { return winner; }
        public Date getCreatedAt() { return createdAt; }
        public List<GuessRecord> getGuessRecords() { return guessRecords; }
        
        public boolean hasPlayer(String username) {
            return players.containsKey(username);
        }
        
        public void addPlayer(String username, String nickname) {
            players.put(username, nickname);
            guessCounts.put(username, 0);
        }
        
        public void removePlayer(String username) {
            players.remove(username);
            guessCounts.remove(username);
        }
        
        public String getPlayerNickname(String username) {
            return players.getOrDefault(username, username);
        }
        
        public int getGuessCount(String username) {
            return guessCounts.getOrDefault(username, 0);
        }
        
        public void generateAnswer() {
            Random random = new Random();
            Set<Integer> digits = new HashSet<>();
            while (digits.size() < 4) {
                digits.add(random.nextInt(10));
            }
            answer = digits.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
        }
        
        public void setGameStarted(boolean started) {
            this.gameStarted = started;
        }
        
        public void setWinner(String username) {
            this.winner = username;
        }
        
        public void addGuessRecord(GuessRecord record) {
            guessRecords.add(record);
            guessCounts.merge(record.getPlayer(), 1, Integer::sum);
        }
        
        public void reset() {
            this.gameStarted = false;
            this.winner = null;
            this.guessRecords.clear();
            this.answer = null;
            guessCounts.clear();
            for (String player : players.keySet()) {
                guessCounts.put(player, 0);
            }
        }
    }
    
    private static class GuessRecord {
        private final String player;
        private final String guess;
        private final int bulls;
        private final int cows;
        private final Date timestamp;
        
        public GuessRecord(String player, String guess, int bulls, int cows) {
            this.player = player;
            this.guess = guess;
            this.bulls = bulls;
            this.cows = cows;
            this.timestamp = new Date();
        }
        
        public String getPlayer() { return player; }
        public String getGuess() { return guess; }
        public int getBulls() { return bulls; }
        public int getCows() { return cows; }
        public Date getTimestamp() { return timestamp; }
    }
}
