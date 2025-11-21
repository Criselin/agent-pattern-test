package com.example.agentpattern.chatbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 机器人回复消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 会话ID
     */
    @JsonProperty("session_id")
    private String sessionId;

    /**
     * 是否成功
     */
    @JsonProperty("success")
    @Builder.Default
    private boolean success = true;

    /**
     * 错误信息（如果失败）
     */
    @JsonProperty("error")
    private String error;

    /**
     * 执行时间（毫秒）
     */
    @JsonProperty("execution_time_ms")
    private Long executionTimeMs;

    /**
     * Agent执行的步骤（调试信息）
     */
    @JsonProperty("steps")
    private List<StepInfo> steps;

    /**
     * 步骤信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepInfo {
        @JsonProperty("thought")
        private String thought;

        @JsonProperty("action")
        private String action;

        @JsonProperty("action_input")
        private String actionInput;

        @JsonProperty("observation")
        private String observation;
    }

    public static ChatResponse success(String message, String sessionId, long executionTimeMs) {
        return ChatResponse.builder()
                .message(message)
                .sessionId(sessionId)
                .success(true)
                .executionTimeMs(executionTimeMs)
                .build();
    }

    public static ChatResponse failure(String error, String sessionId) {
        return ChatResponse.builder()
                .error(error)
                .sessionId(sessionId)
                .success(false)
                .build();
    }
}
