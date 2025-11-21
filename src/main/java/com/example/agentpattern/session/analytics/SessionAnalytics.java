package com.example.agentpattern.session.analytics;

import com.example.agentpattern.session.model.Session;
import com.example.agentpattern.session.repository.SessionRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话分析服务
 * 提供会话统计和洞察分析
 */
@Slf4j
@Service
public class SessionAnalytics {

    private final SessionRepository sessionRepository;

    public SessionAnalytics(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * 获取会话统计信息
     */
    public SessionStats getSessionStats() {
        List<Session> allSessions = sessionRepository.findAll();

        long totalSessions = allSessions.size();
        long activeSessions = allSessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.ACTIVE)
                .count();
        long inactiveSessions = allSessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.INACTIVE)
                .count();
        long expiredSessions = allSessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.EXPIRED)
                .count();

        // 平均会话时长（分钟）
        double avgDuration = allSessions.stream()
                .mapToLong(Session::getDurationMinutes)
                .average()
                .orElse(0.0);

        // 平均消息数
        double avgMessages = allSessions.stream()
                .mapToInt(Session::getTotalMessages)
                .average()
                .orElse(0.0);

        // 总消息数
        long totalMessages = allSessions.stream()
                .mapToInt(Session::getTotalMessages)
                .sum();

        // 唯一用户数
        long uniqueUsers = allSessions.stream()
                .map(Session::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return SessionStats.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .inactiveSessions(inactiveSessions)
                .expiredSessions(expiredSessions)
                .averageDurationMinutes(avgDuration)
                .averageMessagesPerSession(avgMessages)
                .totalMessages(totalMessages)
                .uniqueUsers(uniqueUsers)
                .build();
    }

    /**
     * 获取用户行为分析
     */
    public UserBehaviorAnalytics getUserBehaviorAnalytics(String userId) {
        List<Session> userSessions = sessionRepository.findByUserId(userId);

        if (userSessions.isEmpty()) {
            return UserBehaviorAnalytics.builder()
                    .userId(userId)
                    .totalSessions(0)
                    .build();
        }

        long totalSessions = userSessions.size();
        long totalMessages = userSessions.stream()
                .mapToInt(Session::getTotalMessages)
                .sum();

        double avgMessagesPerSession = (double) totalMessages / totalSessions;

        // 最常使用的编排器
        Map<String, Long> orchestratorUsage = userSessions.stream()
                .map(Session::getOrchestratorType)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(o -> o, Collectors.counting()));

        String mostUsedOrchestrator = orchestratorUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");

        // 最常使用的标签
        Map<String, Long> tagUsage = userSessions.stream()
                .flatMap(s -> s.getTags().stream())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        List<String> topTags = tagUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 最后活跃时间
        LocalDateTime lastActive = userSessions.stream()
                .map(Session::getLastAccessedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return UserBehaviorAnalytics.builder()
                .userId(userId)
                .totalSessions(totalSessions)
                .totalMessages(totalMessages)
                .averageMessagesPerSession(avgMessagesPerSession)
                .mostUsedOrchestrator(mostUsedOrchestrator)
                .topTags(topTags)
                .lastActiveTime(lastActive)
                .build();
    }

    /**
     * 获取热点问题分析
     */
    public HotTopicsAnalytics getHotTopicsAnalytics(int topN) {
        List<Session> allSessions = sessionRepository.findAll();

        // 收集所有用户消息
        Map<String, Long> messageFrequency = new HashMap<>();

        for (Session session : allSessions) {
            for (Session.Message message : session.getMessages()) {
                if (message.getRole() == Session.Message.Role.USER) {
                    String content = message.getContent().toLowerCase();
                    // 简单的关键词提取（可以使用更复杂的NLP）
                    String[] words = content.split("\\s+");
                    for (String word : words) {
                        if (word.length() > 2) { // 过滤短词
                            messageFrequency.merge(word, 1L, Long::sum);
                        }
                    }
                }
            }
        }

        List<TopicCount> hotTopics = messageFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(e -> new TopicCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return HotTopicsAnalytics.builder()
                .hotTopics(hotTopics)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取时间段统计
     */
    public TimeRangeStats getTimeRangeStats(LocalDateTime startTime, LocalDateTime endTime) {
        List<Session> allSessions = sessionRepository.findAll();

        List<Session> sessionsInRange = allSessions.stream()
                .filter(s -> !s.getCreatedAt().isBefore(startTime) && !s.getCreatedAt().isAfter(endTime))
                .collect(Collectors.toList());

        long sessionCount = sessionsInRange.size();
        long messageCount = sessionsInRange.stream()
                .mapToInt(Session::getTotalMessages)
                .sum();

        // 每小时会话数分布
        Map<Integer, Long> sessionsPerHour = sessionsInRange.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        return TimeRangeStats.builder()
                .startTime(startTime)
                .endTime(endTime)
                .sessionCount(sessionCount)
                .messageCount(messageCount)
                .sessionsPerHour(sessionsPerHour)
                .build();
    }

    /**
     * 会话统计数据模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStats {
        private long totalSessions;
        private long activeSessions;
        private long inactiveSessions;
        private long expiredSessions;
        private double averageDurationMinutes;
        private double averageMessagesPerSession;
        private long totalMessages;
        private long uniqueUsers;
    }

    /**
     * 用户行为分析模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBehaviorAnalytics {
        private String userId;
        private long totalSessions;
        private long totalMessages;
        private double averageMessagesPerSession;
        private String mostUsedOrchestrator;
        private List<String> topTags;
        private LocalDateTime lastActiveTime;
    }

    /**
     * 热点话题分析模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotTopicsAnalytics {
        private List<TopicCount> hotTopics;
        private LocalDateTime analysisTime;
    }

    /**
     * 话题统计
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopicCount {
        private String topic;
        private long count;
    }

    /**
     * 时间段统计模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRangeStats {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long sessionCount;
        private long messageCount;
        private Map<Integer, Long> sessionsPerHour;
    }
}
