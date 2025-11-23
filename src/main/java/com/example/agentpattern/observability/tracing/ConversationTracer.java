package com.example.agentpattern.observability.tracing;

import de.langfuse.Langfuse;
import de.langfuse.model.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话追踪器
 * 专门用于追踪整个对话流程
 */
@Slf4j
@Component
public class ConversationTracer {

    private final Tracer tracer;
    private final Langfuse langfuse;
    private final TracingHelper tracingHelper;

    // 存储活跃的追踪上下文
    private final Map<String, TraceContext> activeTraces = new ConcurrentHashMap<>();

    public ConversationTracer(Tracer tracer, Langfuse langfuse, TracingHelper tracingHelper) {
        this.tracer = tracer;
        this.langfuse = langfuse;
        this.tracingHelper = tracingHelper;
    }

    /**
     * 开始追踪对话
     */
    public TraceContext startConversation(String sessionId, String userId, String message) {
        String traceId = UUID.randomUUID().toString();

        // 创建 OpenTelemetry span
        Span span = tracer.spanBuilder("conversation")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        Scope scope = span.makeCurrent();

        span.setAttribute("session.id", sessionId);
        span.setAttribute("user.id", userId != null ? userId : "anonymous");
        span.setAttribute("message.length", message.length());

        // 创建 Langfuse trace
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionId", sessionId);
        metadata.put("userId", userId);
        metadata.put("messageLength", message.length());

        Trace langfuseTrace = null;
        if (langfuse != null) {
            try {
                CreateTraceRequest request = CreateTraceRequest.builder()
                        .id(traceId)
                        .name("conversation")
                        .userId(userId)
                        .metadata(metadata)
                        .input(message)
                        .build();
                langfuseTrace = langfuse.trace(request);
            } catch (Exception e) {
                log.error("Failed to create Langfuse trace", e);
            }
        }

        TraceContext context = TraceContext.builder()
                .traceId(traceId)
                .sessionId(sessionId)
                .otelSpan(span)
                .otelScope(scope)
                .langfuseTrace(langfuseTrace)
                .startTime(Instant.now())
                .build();

        activeTraces.put(sessionId, context);

        return context;
    }

    /**
     * 结束对话追踪
     */
    public void endConversation(String sessionId, String output, boolean success, String error) {
        TraceContext context = activeTraces.remove(sessionId);
        if (context == null) {
            return;
        }

        Instant endTime = Instant.now();

        // 更新 OpenTelemetry span
        if (context.getOtelSpan() != null) {
            context.getOtelSpan().setAttribute("success", success);
            if (output != null) {
                context.getOtelSpan().setAttribute("output.length", output.length());
            }
            if (error != null) {
                context.getOtelSpan().setAttribute("error", error);
            }
            context.getOtelSpan().end();
        }

        if (context.getOtelScope() != null) {
            context.getOtelScope().close();
        }

        // 更新 Langfuse trace
        if (langfuse != null && context.getLangfuseTrace() != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("success", success);
                if (error != null) {
                    metadata.put("error", error);
                }

                UpdateTraceRequest request = UpdateTraceRequest.builder()
                        .id(context.getTraceId())
                        .output(output)
                        .metadata(metadata)
                        .build();

                langfuse.updateTrace(request);
            } catch (Exception e) {
                log.error("Failed to update Langfuse trace", e);
            }
        }

        // 刷新 Langfuse
        if (langfuse != null) {
            langfuse.flush();
        }
    }

    /**
     * 开始追踪编排器
     */
    public SpanContext startOrchestrator(String sessionId, String orchestratorName) {
        TraceContext traceContext = activeTraces.get(sessionId);
        if (traceContext == null) {
            return null;
        }

        String spanId = UUID.randomUUID().toString();

        // 创建 OpenTelemetry span
        Span span = tracer.spanBuilder("orchestrator." + orchestratorName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        Scope scope = span.makeCurrent();

        span.setAttribute("orchestrator.name", orchestratorName);

        // 创建 Langfuse span
        de.langfuse.model.Span langfuseSpan = null;
        if (langfuse != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("orchestratorName", orchestratorName);

                CreateSpanRequest request = CreateSpanRequest.builder()
                        .traceId(traceContext.getTraceId())
                        .id(spanId)
                        .name("orchestrator." + orchestratorName)
                        .startTime(Instant.now())
                        .metadata(metadata)
                        .build();

                langfuseSpan = langfuse.span(request);
            } catch (Exception e) {
                log.error("Failed to create Langfuse span", e);
            }
        }

        return SpanContext.builder()
                .spanId(spanId)
                .traceId(traceContext.getTraceId())
                .otelSpan(span)
                .otelScope(scope)
                .langfuseSpan(langfuseSpan)
                .startTime(Instant.now())
                .build();
    }

    /**
     * 结束编排器追踪
     */
    public void endOrchestrator(SpanContext spanContext, int iterations, boolean success) {
        if (spanContext == null) {
            return;
        }

        Instant endTime = Instant.now();

        // 更新 OpenTelemetry span
        if (spanContext.getOtelSpan() != null) {
            spanContext.getOtelSpan().setAttribute("iterations", iterations);
            spanContext.getOtelSpan().setAttribute("success", success);
            spanContext.getOtelSpan().end();
        }

        if (spanContext.getOtelScope() != null) {
            spanContext.getOtelScope().close();
        }

        // 更新 Langfuse span
        if (langfuse != null && spanContext.getLangfuseSpan() != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("iterations", iterations);
                metadata.put("success", success);

                UpdateSpanRequest request = UpdateSpanRequest.builder()
                        .traceId(spanContext.getTraceId())
                        .id(spanContext.getSpanId())
                        .endTime(endTime)
                        .metadata(metadata)
                        .build();

                langfuse.updateSpan(request);
            } catch (Exception e) {
                log.error("Failed to update Langfuse span", e);
            }
        }
    }

    /**
     * 记录 LLM 调用
     */
    public void recordLLMCall(
            String sessionId,
            String model,
            String prompt,
            String completion,
            Map<String, Object> usage) {

        TraceContext traceContext = activeTraces.get(sessionId);
        if (traceContext == null) {
            return;
        }

        String generationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // 记录到 OpenTelemetry
        Span span = tracer.spanBuilder("llm.call")
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("llm.model", model);
            span.setAttribute("llm.prompt.length", prompt != null ? prompt.length() : 0);
            span.setAttribute("llm.completion.length", completion != null ? completion.length() : 0);

            if (usage != null) {
                usage.forEach((key, value) -> {
                    if (value instanceof Number) {
                        span.setAttribute("llm.usage." + key, ((Number) value).longValue());
                    }
                });
            }
        } finally {
            span.end();
        }

        // 记录到 Langfuse
        if (langfuse != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("model", model);

                CreateGenerationRequest request = CreateGenerationRequest.builder()
                        .traceId(traceContext.getTraceId())
                        .id(generationId)
                        .name("llm.generation")
                        .model(model)
                        .input(prompt)
                        .output(completion)
                        .usage(usage)
                        .metadata(metadata)
                        .startTime(now)
                        .endTime(Instant.now())
                        .build();

                langfuse.generation(request);
            } catch (Exception e) {
                log.error("Failed to record LLM generation to Langfuse", e);
            }
        }
    }

    /**
     * 记录工具调用
     */
    public void recordToolCall(
            String sessionId,
            String toolName,
            String input,
            String output,
            boolean success) {

        TraceContext traceContext = activeTraces.get(sessionId);
        if (traceContext == null) {
            return;
        }

        String spanId = UUID.randomUUID().toString();

        // 记录到 OpenTelemetry
        Span span = tracer.spanBuilder("tool." + toolName)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("tool.name", toolName);
            span.setAttribute("tool.success", success);
            span.setAttribute("tool.input.length", input != null ? input.length() : 0);
            span.setAttribute("tool.output.length", output != null ? output.length() : 0);
        } finally {
            span.end();
        }

        // 记录到 Langfuse
        if (langfuse != null) {
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("toolName", toolName);
                metadata.put("success", success);

                CreateSpanRequest request = CreateSpanRequest.builder()
                        .traceId(traceContext.getTraceId())
                        .id(spanId)
                        .name("tool." + toolName)
                        .input(input)
                        .output(output)
                        .metadata(metadata)
                        .startTime(Instant.now())
                        .endTime(Instant.now())
                        .build();

                langfuse.span(request);
            } catch (Exception e) {
                log.error("Failed to record tool call to Langfuse", e);
            }
        }
    }

    /**
     * 追踪上下文
     */
    @Data
    @Builder
    public static class TraceContext {
        private String traceId;
        private String sessionId;
        private Span otelSpan;
        private Scope otelScope;
        private Trace langfuseTrace;
        private Instant startTime;
    }

    /**
     * Span 上下文
     */
    @Data
    @Builder
    public static class SpanContext {
        private String spanId;
        private String traceId;
        private Span otelSpan;
        private Scope otelScope;
        private de.langfuse.model.Span langfuseSpan;
        private Instant startTime;
    }
}
