package com.example.agentpattern.observability.tracing;

import de.langfuse.Langfuse;
import de.langfuse.model.*;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 追踪辅助类
 * 提供统一的 OpenTelemetry 和 Langfuse 追踪接口
 */
@Slf4j
@Component
public class TracingHelper {

    private final Tracer tracer;
    private final Langfuse langfuse;

    public TracingHelper(Tracer tracer, Langfuse langfuse) {
        this.tracer = tracer;
        this.langfuse = langfuse;
    }

    /**
     * 创建一个追踪 span 并执行操作
     */
    public <T> T traceOperation(String spanName, SpanKind kind, Function<Span, T> operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            T result = operation.apply(span);
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * 创建一个追踪 span 并执行操作（无返回值）
     */
    public void traceOperation(String spanName, SpanKind kind, Consumer<Span> operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            operation.accept(span);
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * 添加属性到当前 span
     */
    public void addAttributes(Span span, Map<String, Object> attributes) {
        if (span == null || attributes == null) {
            return;
        }

        attributes.forEach((key, value) -> {
            if (value instanceof String) {
                span.setAttribute(key, (String) value);
            } else if (value instanceof Long) {
                span.setAttribute(key, (Long) value);
            } else if (value instanceof Integer) {
                span.setAttribute(key, ((Integer) value).longValue());
            } else if (value instanceof Double) {
                span.setAttribute(key, (Double) value);
            } else if (value instanceof Boolean) {
                span.setAttribute(key, (Boolean) value);
            } else if (value != null) {
                span.setAttribute(key, value.toString());
            }
        });
    }

    /**
     * 获取当前 trace ID
     */
    public String getCurrentTraceId() {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            return currentSpan.getSpanContext().getTraceId();
        }
        return null;
    }

    /**
     * 获取当前 span ID
     */
    public String getCurrentSpanId() {
        Span currentSpan = Span.current();
        if (currentSpan != null) {
            return currentSpan.getSpanContext().getSpanId();
        }
        return null;
    }

    /**
     * 创建 Langfuse trace
     */
    public Trace createLangfuseTrace(String traceId, String name, Map<String, Object> metadata) {
        if (langfuse == null) {
            return null;
        }

        try {
            CreateTraceRequest request = CreateTraceRequest.builder()
                    .id(traceId)
                    .name(name)
                    .metadata(metadata)
                    .build();

            return langfuse.trace(request);
        } catch (Exception e) {
            log.error("Failed to create Langfuse trace", e);
            return null;
        }
    }

    /**
     * 创建 Langfuse span
     */
    public de.langfuse.model.Span createLangfuseSpan(
            String traceId,
            String spanId,
            String name,
            Instant startTime,
            Map<String, Object> metadata) {
        if (langfuse == null) {
            return null;
        }

        try {
            CreateSpanRequest request = CreateSpanRequest.builder()
                    .traceId(traceId)
                    .id(spanId)
                    .name(name)
                    .startTime(startTime)
                    .metadata(metadata)
                    .build();

            return langfuse.span(request);
        } catch (Exception e) {
            log.error("Failed to create Langfuse span", e);
            return null;
        }
    }

    /**
     * 记录 LLM 生成到 Langfuse
     */
    public Generation recordLLMGeneration(
            String traceId,
            String generationId,
            String name,
            String model,
            String input,
            String output,
            Map<String, Object> metadata,
            Instant startTime,
            Instant endTime) {
        if (langfuse == null) {
            return null;
        }

        try {
            CreateGenerationRequest request = CreateGenerationRequest.builder()
                    .traceId(traceId)
                    .id(generationId)
                    .name(name)
                    .model(model)
                    .input(input)
                    .output(output)
                    .metadata(metadata)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            return langfuse.generation(request);
        } catch (Exception e) {
            log.error("Failed to record LLM generation to Langfuse", e);
            return null;
        }
    }

    /**
     * 更新 Langfuse span
     */
    public void updateLangfuseSpan(
            String traceId,
            String spanId,
            Instant endTime,
            Map<String, Object> metadata) {
        if (langfuse == null) {
            return;
        }

        try {
            UpdateSpanRequest request = UpdateSpanRequest.builder()
                    .traceId(traceId)
                    .id(spanId)
                    .endTime(endTime)
                    .metadata(metadata)
                    .build();

            langfuse.updateSpan(request);
        } catch (Exception e) {
            log.error("Failed to update Langfuse span", e);
        }
    }

    /**
     * 更新 Langfuse generation
     */
    public void updateLLMGeneration(
            String traceId,
            String generationId,
            String output,
            Map<String, Object> usage,
            Instant endTime) {
        if (langfuse == null) {
            return;
        }

        try {
            UpdateGenerationRequest request = UpdateGenerationRequest.builder()
                    .traceId(traceId)
                    .id(generationId)
                    .output(output)
                    .usage(usage)
                    .endTime(endTime)
                    .build();

            langfuse.updateGeneration(request);
        } catch (Exception e) {
            log.error("Failed to update LLM generation", e);
        }
    }

    /**
     * 刷新 Langfuse 数据
     */
    public void flushLangfuse() {
        if (langfuse != null) {
            langfuse.flush();
        }
    }
}
