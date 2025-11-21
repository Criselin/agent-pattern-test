package com.example.agentpattern.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AI模型配置
 * 配置OpenAI和Azure OpenAI的ChatModel
 */
@Slf4j
@Configuration
public class AIConfig {

    /**
     * OpenAI配置会在application.yml中的spring.ai.openai配置下自动生效
     * Azure OpenAI配置会在application-azure-openai.yml中的spring.ai.azure.openai配置下自动生效
     *
     * Spring AI会自动创建ChatModel bean，这里只是用于日志记录
     */

    @Bean
    @ConditionalOnMissingBean
    @Profile("!test")
    public AIConfigLogger aiConfigLogger(ChatModel chatModel) {
        log.info("=".repeat(60));
        log.info("AI Configuration Initialized");
        log.info("ChatModel Type: {}", chatModel.getClass().getName());
        log.info("=".repeat(60));
        return new AIConfigLogger();
    }

    /**
     * AI配置日志记录器（标记bean）
     */
    public static class AIConfigLogger {
        // 这个bean只是用于标记AI配置已初始化
    }
}
