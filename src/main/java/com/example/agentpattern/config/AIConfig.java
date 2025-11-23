package com.example.agentpattern.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.ai.azure.openai.api-key:NOT_SET}")
    private String azureApiKey;

    @Value("${spring.ai.azure.openai.endpoint:NOT_SET}")
    private String azureEndpoint;

    @Value("${spring.ai.azure.openai.deployment-name:NOT_SET}")
    private String azureDeploymentName;

    @Value("${spring.ai.azure.openai.version:NOT_SET}")
    private String azureVersion;

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

        // 打印 Azure OpenAI 配置（脱敏处理）
        if (chatModel.getClass().getName().contains("AzureOpenAi")) {
            log.info("Azure OpenAI Configuration:");
            log.info("  Endpoint: {}", azureEndpoint);
            log.info("  Deployment Name: {}", azureDeploymentName);
            log.info("  API Version: {}", azureVersion);
            log.info("  API Key: {}...{}",
                    azureApiKey.substring(0, Math.min(10, azureApiKey.length())),
                    azureApiKey.length() > 14 ? azureApiKey.substring(azureApiKey.length() - 4) : "****");
            log.info("=".repeat(60));
        }

        return new AIConfigLogger();
    }

    /**
     * AI配置日志记录器（标记bean）
     */
    public static class AIConfigLogger {
        // 这个bean只是用于标记AI配置已初始化
    }
}
