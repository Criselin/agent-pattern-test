package com.example.agentpattern.chatbot.controller;

import com.example.agentpattern.chatbot.model.ChatRequest;
import com.example.agentpattern.chatbot.model.ChatResponse;
import com.example.agentpattern.chatbot.service.CustomerServiceBot;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 聊天API控制器
 * 提供客服机器人的HTTP接口
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CustomerServiceBot customerServiceBot;

    public ChatController(CustomerServiceBot customerServiceBot) {
        this.customerServiceBot = customerServiceBot;
    }

    /**
     * 聊天接口（同步）
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());

        try {
            ChatResponse response = customerServiceBot.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.failure("系统错误", request.getSessionId()));
        }
    }

    /**
     * 获取欢迎消息
     * GET /api/chat/welcome
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, String>> getWelcome() {
        String welcomeMessage = customerServiceBot.getWelcomeMessage();
        return ResponseEntity.ok(Map.of("message", welcomeMessage));
    }

    /**
     * 清除会话
     * DELETE /api/chat/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        log.info("Clearing session: {}", sessionId);
        customerServiceBot.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session cleared successfully"));
    }

    /**
     * 获取统计信息
     * GET /api/chat/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        int activeSessionCount = customerServiceBot.getActiveSessionCount();
        return ResponseEntity.ok(Map.of(
                "active_sessions", activeSessionCount,
                "status", "running"
        ));
    }

    /**
     * 健康检查
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
