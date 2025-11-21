package com.example.agentpattern.chatbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户消息
     */
    @NotBlank(message = "Message cannot be empty")
    @JsonProperty("message")
    private String message;

    /**
     * 会话ID（可选，用于保持对话上下文）
     */
    @JsonProperty("session_id")
    private String sessionId;

    /**
     * 用户ID（可选）
     */
    @JsonProperty("user_id")
    private String userId;
}
