package com.example.agentpattern.observability.config;

import de.langfuse.Langfuse;
import de.langfuse.config.LangfuseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Langfuse 配置类
 * 配置 Langfuse SDK 用于 LLM 观测
 */
@Slf4j
@Configuration
public class LangfuseConfiguration {

    @Value("${observability.langfuse.secret-key:}")
    private String secretKey;

    @Value("${observability.langfuse.public-key:}")
    private String publicKey;

    @Value("${observability.langfuse.host:https://cloud.langfuse.com}")
    private String host;

    @Value("${observability.langfuse.enabled:true}")
    private boolean enabled;

    private Langfuse langfuse;

    @Bean
    public Langfuse langfuse() {
        if (!enabled) {
            log.info("Langfuse is disabled");
            return null;
        }

        if (secretKey == null || secretKey.isEmpty() || publicKey == null || publicKey.isEmpty()) {
            log.warn("Langfuse credentials not configured. Langfuse tracking will be disabled.");
            return null;
        }

        log.info("Initializing Langfuse with host: {}", host);

        try {
            LangfuseConfig config = LangfuseConfig.builder()
                    .secretKey(secretKey)
                    .publicKey(publicKey)
                    .host(host)
                    .build();

            langfuse = new Langfuse(config);
            log.info("Langfuse initialized successfully");

            return langfuse;
        } catch (Exception e) {
            log.error("Failed to initialize Langfuse", e);
            return null;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (langfuse != null) {
            log.info("Shutting down Langfuse");
            langfuse.shutdown();
        }
    }
}
