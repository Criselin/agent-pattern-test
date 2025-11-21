package com.example.agentpattern.config;

import com.example.agentpattern.agent.orchestrator.core.Orchestrator;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 编排器配置
 * 注册所有可用的编排器
 */
@Slf4j
@Configuration
public class OrchestratorConfig {

    private final OrchestratorRegistry orchestratorRegistry;
    private final List<Orchestrator> orchestrators;

    public OrchestratorConfig(
            OrchestratorRegistry orchestratorRegistry,
            List<Orchestrator> orchestrators
    ) {
        this.orchestratorRegistry = orchestratorRegistry;
        this.orchestrators = orchestrators;
    }

    @PostConstruct
    public void registerOrchestrators() {
        log.info("Registering orchestrators...");

        for (Orchestrator orchestrator : orchestrators) {
            orchestratorRegistry.registerOrchestrator(orchestrator);
        }

        log.info("Total orchestrators registered: {}", orchestratorRegistry.size());
        log.info("=".repeat(60));
        log.info("Orchestration System Initialized");
        log.info(orchestratorRegistry.getOrchestratorsDescription());
        log.info("=".repeat(60));
    }
}
