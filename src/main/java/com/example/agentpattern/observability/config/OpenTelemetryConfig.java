package com.example.agentpattern.observability.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * OpenTelemetry 配置类
 * 配置 OpenTelemetry SDK 和 OTLP Exporter
 */
@Slf4j
@Configuration
public class OpenTelemetryConfig {

    @Value("${observability.otel.service-name:agent-pattern-test}")
    private String serviceName;

    @Value("${observability.otel.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    @Value("${observability.otel.enabled:true}")
    private boolean enabled;

    private SdkTracerProvider tracerProvider;

    @Bean
    public OpenTelemetry openTelemetry() {
        if (!enabled) {
            log.info("OpenTelemetry is disabled");
            return OpenTelemetry.noop();
        }

        log.info("Initializing OpenTelemetry with service name: {} and endpoint: {}",
                serviceName, otlpEndpoint);

        // 创建资源属性
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.builder()
                                .put(AttributeKey.stringKey("service.name"), serviceName)
                                .put(AttributeKey.stringKey("service.version"), "1.0.0")
                                .build()
                ));

        // 创建 OTLP Exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();

        // 创建 TracerProvider
        tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(resource)
                .build();

        // 创建 OpenTelemetry 实例
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        log.info("OpenTelemetry initialized successfully");

        return openTelemetry;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("com.example.agentpattern", "1.0.0");
    }

    @PreDestroy
    public void cleanup() {
        if (tracerProvider != null) {
            log.info("Shutting down OpenTelemetry TracerProvider");
            tracerProvider.close();
        }
    }
}
