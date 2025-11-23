package com.example.agentpattern.observability.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Langfuse 配置类
 * 配置 Langfuse SDK 用于 LLM 观测
 *
 * NOTE: Langfuse SDK is currently disabled as it's not available in Maven Central.
 * This is a stub implementation that disables Langfuse functionality.
 */
@Slf4j
@Configuration
public class LangfuseConfiguration {

    @Value("${observability.langfuse.enabled:false}")
    private boolean enabled;

    @Bean
    public Object langfuse() {
        if (!enabled) {
            log.info("Langfuse is disabled");
            return null;
        }

        log.warn("Langfuse SDK is not available. Langfuse tracking will be disabled.");
        return null;
    }
}
