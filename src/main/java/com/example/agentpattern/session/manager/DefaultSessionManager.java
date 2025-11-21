package com.example.agentpattern.session.manager;

import com.example.agentpattern.session.model.Session;
import com.example.agentpattern.session.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 默认会话管理器实现
 * 提供完整的会话生命周期管理
 */
@Slf4j
@Service
public class DefaultSessionManager implements SessionManager {

    private final SessionRepository sessionRepository;

    @Value("${session.default-ttl-minutes:60}")
    private int defaultTtlMinutes;

    @Value("${session.auto-cleanup:true}")
    private boolean autoCleanup;

    public DefaultSessionManager(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session createSession(String userId) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(defaultTtlMinutes);
        return createSession(userId, expiresAt);
    }

    @Override
    public Session createSession(String userId, LocalDateTime expiresAt) {
        String sessionId = generateSessionId();

        Session session = Session.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status(Session.SessionStatus.ACTIVE)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        sessionRepository.save(session);
        log.info("Created new session: {} for user: {}", sessionId, userId);

        return session;
    }

    @Override
    public Optional<Session> getSession(String sessionId) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);

        sessionOpt.ifPresent(session -> {
            // 更新最后访问时间
            session.updateLastAccessed();

            // 检查是否过期
            if (session.isExpired()) {
                session.setStatus(Session.SessionStatus.EXPIRED);
                log.warn("Session {} has expired", sessionId);
            }

            sessionRepository.save(session);
        });

        return sessionOpt;
    }

    @Override
    public void updateSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        log.debug("Updated session: {}", session.getSessionId());
    }

    @Override
    public boolean deleteSession(String sessionId) {
        boolean deleted = sessionRepository.delete(sessionId);
        if (deleted) {
            log.info("Deleted session: {}", sessionId);
        }
        return deleted;
    }

    @Override
    public List<Session> listAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public List<Session> listSessionsByUser(String userId) {
        return sessionRepository.findByUserId(userId);
    }

    @Override
    public List<Session> listSessionsByStatus(Session.SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }

    @Override
    public long getActiveSessionCount() {
        return sessionRepository.countByStatus(Session.SessionStatus.ACTIVE);
    }

    @Override
    public int cleanupExpiredSessions() {
        List<Session> allSessions = sessionRepository.findAll();
        int cleanedCount = 0;

        for (Session session : allSessions) {
            if (session.isExpired()) {
                session.setStatus(Session.SessionStatus.EXPIRED);
                sessionRepository.save(session);
                cleanedCount++;
            }
        }

        log.info("Cleaned up {} expired sessions", cleanedCount);
        return cleanedCount;
    }

    @Override
    public void clearAllSessions() {
        sessionRepository.deleteAll();
        log.info("Cleared all sessions");
    }

    @Override
    public boolean sessionExists(String sessionId) {
        return sessionRepository.exists(sessionId);
    }

    @Override
    public void updateSessionStatus(String sessionId, Session.SessionStatus status) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        sessionOpt.ifPresent(session -> {
            session.setStatus(status);
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.info("Updated session {} status to {}", sessionId, status);
        });
    }

    @Override
    public void addMessage(String sessionId, Session.Message message) {
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        sessionOpt.ifPresent(session -> {
            session.addMessage(message);
            sessionRepository.save(session);
            log.debug("Added message to session: {}", sessionId);
        });
    }

    @Override
    public long getTotalSessionCount() {
        return sessionRepository.count();
    }

    /**
     * 定时清理过期会话（每小时执行一次）
     */
    @Scheduled(cron = "${session.cleanup-cron:0 0 * * * ?}")
    public void scheduledCleanup() {
        if (autoCleanup) {
            log.info("Starting scheduled session cleanup...");
            int cleaned = cleanupExpiredSessions();
            log.info("Scheduled cleanup completed: {} sessions marked as expired", cleaned);
        }
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session-" + UUID.randomUUID().toString();
    }
}
