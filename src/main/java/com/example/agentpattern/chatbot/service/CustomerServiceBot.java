package com.example.agentpattern.chatbot.service;

import com.example.agentpattern.agent.core.Agent;
import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.chatbot.model.ChatRequest;
import com.example.agentpattern.chatbot.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 客服机器人服务
 * 使用ReAct Agent提供智能客服功能
 */
@Slf4j
@Service
public class CustomerServiceBot {

    private final Agent reactAgent;

    @Value("${chatbot.name:智能客服助手}")
    private String botName;

    @Value("${chatbot.welcome-message:您好！我是智能客服助手,很高兴为您服务。}")
    private String welcomeMessage;

    @Value("${agent.react.max-iterations:5}")
    private int maxIterations;

    // 会话存储（实际应用中应使用Redis等持久化存储）
    private final Map<String, AgentContext> sessionStore = new ConcurrentHashMap<>();

    public CustomerServiceBot(Agent reactAgent) {
        this.reactAgent = reactAgent;
    }

    /**
     * 处理聊天请求
     */
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 获取或创建会话
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = generateSessionId();
                log.info("Created new session: {}", sessionId);
            }

            // 构建或获取上下文
            AgentContext context = getOrCreateContext(sessionId, request.getMessage());

            log.info("Processing chat request - Session: {}, Message: {}", sessionId, request.getMessage());

            // 执行Agent
            Agent.AgentResponse agentResponse = reactAgent.execute(context);

            // 更新会话存储
            sessionStore.put(sessionId, agentResponse.getContext());

            long executionTime = System.currentTimeMillis() - startTime;

            if (agentResponse.isSuccess()) {
                log.info("Chat completed successfully - Session: {}, Time: {}ms", sessionId, executionTime);

                // 构建响应（包含步骤信息用于调试）
                List<ChatResponse.StepInfo> steps = agentResponse.getContext().getSteps().stream()
                        .map(step -> ChatResponse.StepInfo.builder()
                                .thought(step.getThought())
                                .action(step.getAction())
                                .actionInput(step.getActionInput())
                                .observation(step.getObservation())
                                .build())
                        .collect(Collectors.toList());

                return ChatResponse.builder()
                        .message(agentResponse.getAnswer())
                        .sessionId(sessionId)
                        .success(true)
                        .executionTimeMs(executionTime)
                        .steps(steps)
                        .build();
            } else {
                log.error("Chat failed - Session: {}, Error: {}", sessionId, agentResponse.getError());
                return ChatResponse.failure(
                        "抱歉，处理您的请求时遇到问题：" + agentResponse.getError(),
                        sessionId
                );
            }

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return ChatResponse.builder()
                    .error("系统错误：" + e.getMessage())
                    .sessionId(request.getSessionId())
                    .success(false)
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    /**
     * 获取欢迎消息
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * 清除会话
     */
    public void clearSession(String sessionId) {
        sessionStore.remove(sessionId);
        log.info("Cleared session: {}", sessionId);
    }

    /**
     * 获取活跃会话数
     */
    public int getActiveSessionCount() {
        return sessionStore.size();
    }

    /**
     * 获取或创建上下文
     */
    private AgentContext getOrCreateContext(String sessionId, String input) {
        AgentContext existingContext = sessionStore.get(sessionId);

        if (existingContext != null) {
            // 更新输入，保留历史步骤
            existingContext.setInput(input);
            existingContext.setCurrentIteration(0); // 重置迭代计数
            return existingContext;
        }

        // 创建新上下文
        return AgentContext.builder()
                .sessionId(sessionId)
                .input(input)
                .maxIterations(maxIterations)
                .build();
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session-" + UUID.randomUUID().toString();
    }
}
