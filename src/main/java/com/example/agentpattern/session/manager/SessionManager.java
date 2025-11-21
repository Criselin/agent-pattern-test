package com.example.agentpattern.session.manager;

import com.example.agentpattern.session.model.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会话管理器接口
 * 定义会话的生命周期管理和查询操作
 */
public interface SessionManager {

    /**
     * 创建新会话
     *
     * @param userId 用户ID（可选）
     * @return 新创建的会话
     */
    Session createSession(String userId);

    /**
     * 创建新会话（带过期时间）
     *
     * @param userId 用户ID
     * @param expiresAt 过期时间
     * @return 新创建的会话
     */
    Session createSession(String userId, LocalDateTime expiresAt);

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return 会话（如果存在）
     */
    Optional<Session> getSession(String sessionId);

    /**
     * 更新会话
     *
     * @param session 会话对象
     */
    void updateSession(Session session);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 是否成功删除
     */
    boolean deleteSession(String sessionId);

    /**
     * 列出所有会话
     *
     * @return 会话列表
     */
    List<Session> listAllSessions();

    /**
     * 根据用户ID列出会话
     *
     * @param userId 用户ID
     * @return 用户的会话列表
     */
    List<Session> listSessionsByUser(String userId);

    /**
     * 根据状态列出会话
     *
     * @param status 会话状态
     * @return 指定状态的会话列表
     */
    List<Session> listSessionsByStatus(Session.SessionStatus status);

    /**
     * 获取活跃会话数
     *
     * @return 活跃会话数量
     */
    long getActiveSessionCount();

    /**
     * 清理过期会话
     *
     * @return 清理的会话数量
     */
    int cleanupExpiredSessions();

    /**
     * 清理所有会话
     */
    void clearAllSessions();

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean sessionExists(String sessionId);

    /**
     * 更新会话状态
     *
     * @param sessionId 会话ID
     * @param status 新状态
     */
    void updateSessionStatus(String sessionId, Session.SessionStatus status);

    /**
     * 添加消息到会话
     *
     * @param sessionId 会话ID
     * @param message 消息
     */
    void addMessage(String sessionId, Session.Message message);

    /**
     * 获取会话总数
     *
     * @return 总会话数
     */
    long getTotalSessionCount();
}
