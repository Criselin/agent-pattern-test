package com.example.agentpattern.session.controller;

import com.example.agentpattern.session.analytics.SessionAnalytics;
import com.example.agentpattern.session.manager.SessionManager;
import com.example.agentpattern.session.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 会话管理Controller
 * 提供会话的CRUD操作和分析功能
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionManager sessionManager;
    private final SessionAnalytics sessionAnalytics;

    public SessionController(SessionManager sessionManager, SessionAnalytics sessionAnalytics) {
        this.sessionManager = sessionManager;
        this.sessionAnalytics = sessionAnalytics;
    }

    /**
     * 获取所有会话列表
     */
    @GetMapping
    public ResponseEntity<List<Session>> listAllSessions() {
        log.info("Fetching all sessions");
        List<Session> sessions = sessionManager.listAllSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * 根据ID获取会话详情
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        log.info("Fetching session: {}", sessionId);
        Optional<Session> session = sessionManager.getSession(sessionId);

        if (session.isPresent()) {
            return ResponseEntity.ok(session.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取指定用户的所有会话
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Session>> getUserSessions(@PathVariable String userId) {
        log.info("Fetching sessions for user: {}", userId);
        List<Session> sessions = sessionManager.listSessionsByUser(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 根据状态获取会话列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Session>> getSessionsByStatus(@PathVariable Session.SessionStatus status) {
        log.info("Fetching sessions with status: {}", status);
        List<Session> sessions = sessionManager.listSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 创建新会话
     */
    @PostMapping
    public ResponseEntity<Session> createSession(
            @RequestParam String userId,
            @RequestParam(required = false) Integer ttlMinutes
    ) {
        log.info("Creating new session for user: {}", userId);

        Session session;
        if (ttlMinutes != null) {
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);
            session = sessionManager.createSession(userId, expiresAt);
        } else {
            session = sessionManager.createSession(userId);
        }

        return ResponseEntity.ok(session);
    }

    /**
     * 更新会话状态
     */
    @PutMapping("/{sessionId}/status")
    public ResponseEntity<Void> updateSessionStatus(
            @PathVariable String sessionId,
            @RequestParam Session.SessionStatus status
    ) {
        log.info("Updating session {} status to {}", sessionId, status);
        sessionManager.updateSessionStatus(sessionId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        log.info("Deleting session: {}", sessionId);
        boolean deleted = sessionManager.deleteSession(sessionId);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session deleted successfully"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 清空所有会话
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllSessions() {
        log.info("Clearing all sessions");
        sessionManager.clearAllSessions();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All sessions cleared"
        ));
    }

    /**
     * 手动触发过期会话清理
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredSessions() {
        log.info("Manual cleanup of expired sessions triggered");
        int cleanedCount = sessionManager.cleanupExpiredSessions();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "cleanedCount", cleanedCount,
            "message", "Cleaned " + cleanedCount + " expired sessions"
        ));
    }

    /**
     * 获取会话统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats() {
        log.info("Fetching session statistics");
        SessionAnalytics.SessionStats stats = sessionAnalytics.getSessionStats();

        return ResponseEntity.ok(Map.of(
            "totalSessions", stats.getTotalSessions(),
            "activeSessions", stats.getActiveSessions(),
            "inactiveSessions", stats.getInactiveSessions(),
            "expiredSessions", stats.getExpiredSessions(),
            "averageSessionDurationMinutes", stats.getAverageSessionDurationMinutes(),
            "averageMessagesPerSession", stats.getAverageMessagesPerSession(),
            "uniqueUsers", stats.getUniqueUsers()
        ));
    }

    /**
     * 获取用户行为分析
     */
    @GetMapping("/analytics/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable String userId) {
        log.info("Fetching analytics for user: {}", userId);
        SessionAnalytics.UserBehaviorAnalytics analytics = sessionAnalytics.getUserBehaviorAnalytics(userId);

        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "userId", analytics.getUserId(),
            "totalSessions", analytics.getTotalSessions(),
            "totalMessages", analytics.getTotalMessages(),
            "mostUsedOrchestrator", analytics.getMostUsedOrchestrator() != null ? analytics.getMostUsedOrchestrator() : "N/A",
            "topTags", analytics.getTopTags(),
            "lastActiveAt", analytics.getLastActiveAt() != null ? analytics.getLastActiveAt().toString() : "N/A"
        ));
    }

    /**
     * 获取热门话题分析
     */
    @GetMapping("/analytics/hot-topics")
    public ResponseEntity<Map<String, Object>> getHotTopics(
            @RequestParam(defaultValue = "10") int topN
    ) {
        log.info("Fetching top {} hot topics", topN);
        SessionAnalytics.HotTopicsAnalytics hotTopics = sessionAnalytics.getHotTopicsAnalytics(topN);

        return ResponseEntity.ok(Map.of(
            "totalMessages", hotTopics.getTotalMessages(),
            "uniqueKeywords", hotTopics.getUniqueKeywords(),
            "topKeywords", hotTopics.getTopKeywords()
        ));
    }

    /**
     * 获取时间范围内的统计
     */
    @GetMapping("/analytics/time-range")
    public ResponseEntity<Map<String, Object>> getTimeRangeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        log.info("Fetching stats for time range: {} to {}", startTime, endTime);
        SessionAnalytics.TimeRangeStats stats = sessionAnalytics.getTimeRangeStats(startTime, endTime);

        return ResponseEntity.ok(Map.of(
            "startTime", stats.getStartTime().toString(),
            "endTime", stats.getEndTime().toString(),
            "sessionCount", stats.getSessionCount(),
            "messageCount", stats.getMessageCount(),
            "sessionsPerHour", stats.getSessionsPerHour()
        ));
    }

    /**
     * 检查会话是否存在
     */
    @GetMapping("/{sessionId}/exists")
    public ResponseEntity<Map<String, Boolean>> sessionExists(@PathVariable String sessionId) {
        boolean exists = sessionManager.sessionExists(sessionId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 获取活跃会话数量
     */
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> getActiveSessionCount() {
        long count = sessionManager.getActiveSessionCount();
        return ResponseEntity.ok(Map.of("activeSessionCount", count));
    }

    /**
     * 获取总会话数量
     */
    @GetMapping("/count/total")
    public ResponseEntity<Map<String, Long>> getTotalSessionCount() {
        long count = sessionManager.getTotalSessionCount();
        return ResponseEntity.ok(Map.of("totalSessionCount", count));
    }
}
