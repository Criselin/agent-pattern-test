package com.example.agentpattern.session.repository;

import com.example.agentpattern.session.model.Session;

import java.util.List;
import java.util.Optional;

/**
 * 会话存储接口
 * 定义会话的持久化操作
 */
public interface SessionRepository {

    /**
     * 保存会话
     */
    void save(Session session);

    /**
     * 根据ID查找会话
     */
    Optional<Session> findById(String sessionId);

    /**
     * 根据用户ID查找会话
     */
    List<Session> findByUserId(String userId);

    /**
     * 根据状态查找会话
     */
    List<Session> findByStatus(Session.SessionStatus status);

    /**
     * 查找所有会话
     */
    List<Session> findAll();

    /**
     * 删除会话
     */
    boolean delete(String sessionId);

    /**
     * 删除所有会话
     */
    void deleteAll();

    /**
     * 检查会话是否存在
     */
    boolean exists(String sessionId);

    /**
     * 统计会话总数
     */
    long count();

    /**
     * 统计指定状态的会话数
     */
    long countByStatus(Session.SessionStatus status);
}
