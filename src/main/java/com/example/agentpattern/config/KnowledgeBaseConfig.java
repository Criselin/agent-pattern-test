package com.example.agentpattern.config;

import com.example.agentpattern.knowledge.base.KnowledgeBase;
import com.example.agentpattern.knowledge.base.KnowledgeBaseRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * 知识库配置
 */
@Slf4j
@Configuration
public class KnowledgeBaseConfig {

    private final KnowledgeBaseRegistry knowledgeBaseRegistry;

    public KnowledgeBaseConfig(KnowledgeBaseRegistry knowledgeBaseRegistry) {
        this.knowledgeBaseRegistry = knowledgeBaseRegistry;
    }

    /**
     * 应用启动完成后打印知识库信息
     */
    @EventListener(ApplicationReadyEvent.class)
    public void logKnowledgeBaseInfo() {
        log.info("=".repeat(60));
        log.info("Knowledge Base System Initialized");
        log.info("Total Knowledge Bases: {}", knowledgeBaseRegistry.size());

        for (KnowledgeBase kb : knowledgeBaseRegistry.getAllKnowledgeBases()) {
            log.info("  - {} ({}): {} documents",
                    kb.getName(),
                    kb.getType(),
                    kb.getDocumentCount());
        }

        log.info("=".repeat(60));
    }
}
