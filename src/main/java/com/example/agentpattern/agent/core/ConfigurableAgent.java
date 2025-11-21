package com.example.agentpattern.agent.core;

import com.example.agentpattern.agent.orchestrator.core.Orchestrator;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorRegistry;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 可配置Agent
 * 支持动态切换编排器策略
 */
@Slf4j
@Component("configurableAgent")
public class ConfigurableAgent implements Agent {

    private final OrchestratorRegistry orchestratorRegistry;

    @Value("${agent.orchestrator.default:react}")
    private String defaultOrchestratorName;

    public ConfigurableAgent(OrchestratorRegistry orchestratorRegistry) {
        this.orchestratorRegistry = orchestratorRegistry;
    }

    @Override
    public String getName() {
        return "ConfigurableAgent";
    }

    @Override
    public String getDescription() {
        return "Configurable Agent that supports multiple orchestration strategies";
    }

    @Override
    public AgentResponse execute(String input) {
        AgentContext context = AgentContext.builder()
                .input(input)
                .maxIterations(5)
                .build();
        return execute(context);
    }

    @Override
    public AgentResponse execute(AgentContext context) {
        long startTime = System.currentTimeMillis();

        try {
            // 从上下文中获取编排器名称，如果没有则使用默认
            String orchestratorName = (String) context.getVariable("orchestrator");
            if (orchestratorName == null || orchestratorName.isEmpty()) {
                orchestratorName = defaultOrchestratorName;
            }

            log.info("Using orchestrator: {}", orchestratorName);

            // 获取编排器
            Orchestrator orchestrator = orchestratorRegistry.getOrchestrator(orchestratorName)
                    .orElse(null);

            if (orchestrator == null) {
                log.error("Orchestrator not found: {}", orchestratorName);
                String availableOrchestrators = String.join(", ", orchestratorRegistry.getOrchestratorNames());
                long executionTime = System.currentTimeMillis() - startTime;
                return AgentResponse.failure(
                        "Orchestrator '" + orchestratorName + "' not found. Available: " + availableOrchestrators,
                        context,
                        executionTime
                );
            }

            // 执行编排
            OrchestratorResult result = orchestrator.orchestrate(context);

            // 转换为AgentResponse
            long executionTime = System.currentTimeMillis() - startTime;

            if (result.isSuccess()) {
                return AgentResponse.success(result.getAnswer(), context, executionTime);
            } else {
                return AgentResponse.failure(result.getError(), context, executionTime);
            }

        } catch (Exception e) {
            log.error("Error executing ConfigurableAgent", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return AgentResponse.failure("Error: " + e.getMessage(), context, executionTime);
        }
    }

    /**
     * 使用指定编排器执行
     */
    public AgentResponse executeWithOrchestrator(String input, String orchestratorName) {
        AgentContext context = AgentContext.builder()
                .input(input)
                .maxIterations(5)
                .build();
        context.setVariable("orchestrator", orchestratorName);
        return execute(context);
    }

    /**
     * 获取可用的编排器列表
     */
    public String getAvailableOrchestrators() {
        return orchestratorRegistry.getOrchestratorsDescription();
    }
}
