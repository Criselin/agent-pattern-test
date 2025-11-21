package com.example.agentpattern.session.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话模型
 * 记录完整的用户对话会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID（可选）
     */
    private String userId;

    /**
     * 会话状态
     */
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    /**
     * 对话消息历史
     */
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * 会话元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 会话标签
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 最后更新时间
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 最后访问时间
     */
    @Builder.Default
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    /**
     * 过期时间（可选）
     */
    private LocalDateTime expiresAt;

    /**
     * 总消息数
     */
    @Builder.Default
    private int totalMessages = 0;

    /**
     * 使用的编排器
     */
    private String orchestratorType;

    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 添加消息
     */
    public void addMessage(Message message) {
        messages.add(message);
        totalMessages++;
        updatedAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }

    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
        updatedAt = LocalDateTime.now();
    }

    /**
     * 添加标签
     */
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccessed() {
        lastAccessedAt = LocalDateTime.now();
    }

    /**
     * 检查会话是否过期
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 获取会话持续时间（分钟）
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(createdAt, updatedAt).toMinutes();
    }

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        ACTIVE,     // 活跃
        INACTIVE,   // 不活跃
        EXPIRED,    // 过期
        CLOSED      // 已关闭
    }

    /**
     * 消息模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 消息ID
         */
        private String messageId;

        /**
         * 角色（user/assistant）
         */
        private Role role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 时间戳
         */
        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();

        /**
         * 执行时间（毫秒）
         */
        private Long executionTimeMs;

        /**
         * 使用的工具
         */
        private List<String> toolsUsed;

        /**
         * 执行步骤数
         */
        private Integer stepCount;

        /**
         * 是否成功
         */
        @Builder.Default
        private boolean success = true;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 角色枚举
         */
        public enum Role {
            USER,       // 用户
            ASSISTANT,  // 助手
            SYSTEM      // 系统
        }
    }
}
