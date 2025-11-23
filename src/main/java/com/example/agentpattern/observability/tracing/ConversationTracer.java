package com.example.agentpattern.observability.tracing;

import io.opentelemetry.api.trace.Tracer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 对话追踪器 - 存根实现
 *
 * NOTE: This is a stub implementation as Langfuse SDK is not available in Maven Central.
 * OpenTelemetry tracing is still functional.
 */
@Slf4j
@Component
public class ConversationTracer {

    private final Tracer tracer;

    public ConversationTracer(Tracer tracer) {
        this.tracer = tracer;
        log.info("ConversationTracer initialized (stub mode - Langfuse disabled)");
    }

    /**
     * 开始追踪对话
     */
    public TraceContext startConversation(String sessionId, String userId, String userMessage) {
        log.debug("Starting conversation trace - Session: {}, User: {}", sessionId, userId);

        // 只使用 OpenTelemetry 追踪
        io.opentelemetry.api.trace.Span otelSpan = tracer.spanBuilder("conversation")
                .setAttribute("session.id", sessionId)
                .setAttribute("user.id", userId)
                .setAttribute("user.message", userMessage)
                .startSpan();

        return TraceContext.builder()
                .sessionId(sessionId)
                .otelSpan(otelSpan)
                .build();
    }

    /**
     * 结束对话追踪
     */
    public void endConversation(String sessionId, String response, boolean success, String error) {
        log.debug("Ending conversation trace - Session: {}, Success: {}", sessionId, success);
        // Stub implementation - no operation needed
    }

    /**
     * 开始编排器追踪
     */
    public SpanContext startOrchestrator(String sessionId, String orchestratorType) {
        log.debug("Starting orchestrator trace - Session: {}, Type: {}", sessionId, orchestratorType);

        io.opentelemetry.api.trace.Span otelSpan = tracer.spanBuilder("orchestrator")
                .setAttribute("session.id", sessionId)
                .setAttribute("orchestrator.type", orchestratorType)
                .startSpan();

        return SpanContext.builder()
                .spanId(sessionId + "-orchestrator")
                .otelSpan(otelSpan)
                .build();
    }

    /**
     * 记录 LLM 调用
     */
    public void recordLLMCall(String sessionId, String model, String prompt, String response, java.util.Map<String, Object> metadata) {
        log.debug("Recording LLM call - Session: {}, Model: {}", sessionId, model);
        // Stub implementation - no operation needed
    }

    /**
     * 结束编排器追踪
     */
    public void endOrchestrator(SpanContext spanContext, int iterations, boolean success) {
        log.debug("Ending orchestrator trace - Success: {}, Iterations: {}", success, iterations);
        if (spanContext != null && spanContext.getOtelSpan() != null) {
            spanContext.getOtelSpan().end();
        }
    }

    /**
     * 记录工具调用
     */
    public void recordToolCall(String sessionId, String toolName, String input, String output, boolean success) {
        log.debug("Recording tool call - Session: {}, Tool: {}, Success: {}", sessionId, toolName, success);
        // Stub implementation - no operation needed
    }

    /**
     * 追踪上下文
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class TraceContext {
        private String sessionId;
        private io.opentelemetry.api.trace.Span otelSpan;
    }

    /**
     * Span上下文 - 存根
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class SpanContext {
        private String spanId;
        private io.opentelemetry.api.trace.Span otelSpan;
    }
}
