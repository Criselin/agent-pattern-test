package com.example.agentpattern.chatbot.service;

import com.example.agentpattern.agent.core.Agent;
import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.chatbot.model.ChatRequest;
import com.example.agentpattern.chatbot.model.ChatResponse;
import com.example.agentpattern.observability.tracing.ConversationTracer;
import com.example.agentpattern.session.manager.SessionManager;
import com.example.agentpattern.session.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 客服机器人服务
 * 使用Agent提供智能客服功能，并集成完整的会话管理
 */
@Slf4j
@Service
public class CustomerServiceBot {

    private final Agent reactAgent;
    private final SessionManager sessionManager;
    private final ConversationTracer conversationTracer;

    @Value("${chatbot.name:智能客服助手}")
    private String botName;

    @Value("${chatbot.welcome-message:您好！我是智能客服助手,很高兴为您服务。}")
    private String welcomeMessage;

    @Value("${agent.react.max-iterations:5}")
    private int maxIterations;

    public CustomerServiceBot(Agent reactAgent, SessionManager sessionManager, ConversationTracer conversationTracer) {
        this.reactAgent = reactAgent;
        this.sessionManager = sessionManager;
        this.conversationTracer = conversationTracer;
    }

    /**
     * 处理聊天请求
     */
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // 开始追踪对话
        String tempSessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        ConversationTracer.TraceContext traceContext = conversationTracer.startConversation(
                tempSessionId,
                request.getUserId(),
                request.getMessage()
        );

        try {
            // 获取或创建会话
            String sessionId = request.getSessionId();
            Session session;

            if (sessionId == null || sessionId.isEmpty()) {
                // 创建新会话
                session = sessionManager.createSession(request.getUserId());
                sessionId = session.getSessionId();
                log.info("Created new session: {} for user: {}", sessionId, request.getUserId());
            } else {
                // 获取现有会话
                session = sessionManager.getSession(sessionId)
                        .orElseGet(() -> {
                            log.warn("Session {} not found, creating new one", sessionId);
                            return sessionManager.createSession(request.getUserId());
                        });
            }

            // 记录用户消息
            Session.Message userMessage = Session.Message.builder()
                    .messageId(UUID.randomUUID().toString())
                    .role(Session.Message.Role.USER)
                    .content(request.getMessage())
                    .build();
            session.addMessage(userMessage);

            // 构建Agent上下文
            AgentContext context = buildAgentContext(session, request.getMessage());

            log.info("Processing chat request - Session: {}, User: {}, Message: {}",
                    sessionId, request.getUserId(), request.getMessage());

            // 执行Agent
            Agent.AgentResponse agentResponse = reactAgent.execute(context);

            long executionTime = System.currentTimeMillis() - startTime;

            if (agentResponse.isSuccess()) {
                log.info("Chat completed successfully - Session: {}, Time: {}ms", sessionId, executionTime);

                // 记录助手消息
                List<String> toolsUsed = agentResponse.getContext().getSteps().stream()
                        .map(AgentContext.AgentStep::getAction)
                        .filter(action -> action != null && !action.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                Session.Message assistantMessage = Session.Message.builder()
                        .messageId(UUID.randomUUID().toString())
                        .role(Session.Message.Role.ASSISTANT)
                        .content(agentResponse.getAnswer())
                        .executionTimeMs(executionTime)
                        .toolsUsed(toolsUsed)
                        .stepCount(agentResponse.getContext().getSteps().size())
                        .success(true)
                        .build();
                session.addMessage(assistantMessage);

                // 更新会话元数据
                String orchestrator = (String) agentResponse.getContext().getVariable("orchestrator");
                if (orchestrator != null) {
                    session.setOrchestratorType(orchestrator);
                }

                // 保存会话
                sessionManager.updateSession(session);

                // 结束追踪（成功）
                conversationTracer.endConversation(sessionId, agentResponse.getAnswer(), true, null);

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

                // 记录失败的助手消息
                Session.Message assistantMessage = Session.Message.builder()
                        .messageId(UUID.randomUUID().toString())
                        .role(Session.Message.Role.ASSISTANT)
                        .content("处理失败")
                        .executionTimeMs(executionTime)
                        .success(false)
                        .error(agentResponse.getError())
                        .build();
                session.addMessage(assistantMessage);

                // 保存会话
                sessionManager.updateSession(session);

                // 结束追踪（失败）
                conversationTracer.endConversation(sessionId, null, false, agentResponse.getError());

                return ChatResponse.failure(
                        "抱歉，处理您的请求时遇到问题：" + agentResponse.getError(),
                        sessionId
                );
            }

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            long executionTime = System.currentTimeMillis() - startTime;

            // 结束追踪（异常）
            conversationTracer.endConversation(tempSessionId, null, false, e.getMessage());

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
        sessionManager.deleteSession(sessionId);
        log.info("Cleared session: {}", sessionId);
    }

    /**
     * 获取活跃会话数
     */
    public long getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }

    /**
     * 构建Agent上下文
     */
    private AgentContext buildAgentContext(Session session, String input) {
        return AgentContext.builder()
                .sessionId(session.getSessionId())
                .input(input)
                .maxIterations(maxIterations)
                .build();
    }
}
