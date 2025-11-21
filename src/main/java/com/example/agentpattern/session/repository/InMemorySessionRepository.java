package com.example.agentpattern.session.repository;

import com.example.agentpattern.session.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于内存的会话存储实现
 * 适用于单机部署，可扩展为Redis/MySQL实现
 */
@Slf4j
@Repository
public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(Session session) {
        if (session == null || session.getSessionId() == null) {
            throw new IllegalArgumentException("Session and sessionId cannot be null");
        }
        sessions.put(session.getSessionId(), session);
        log.debug("Saved session: {}", session.getSessionId());
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<Session> findByUserId(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return sessions.values().stream()
                .filter(session -> userId.equals(session.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> findByStatus(Session.SessionStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return sessions.values().stream()
                .filter(session -> status.equals(session.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> findAll() {
        return new ArrayList<>(sessions.values());
    }

    @Override
    public boolean delete(String sessionId) {
        boolean removed = sessions.remove(sessionId) != null;
        if (removed) {
            log.debug("Deleted session: {}", sessionId);
        }
        return removed;
    }

    @Override
    public void deleteAll() {
        int count = sessions.size();
        sessions.clear();
        log.info("Deleted all sessions: {} sessions cleared", count);
    }

    @Override
    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Override
    public long count() {
        return sessions.size();
    }

    @Override
    public long countByStatus(Session.SessionStatus status) {
        if (status == null) {
            return 0;
        }
        return sessions.values().stream()
                .filter(session -> status.equals(session.getStatus()))
                .count();
    }
}
