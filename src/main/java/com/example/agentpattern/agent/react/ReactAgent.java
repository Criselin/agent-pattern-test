package com.example.agentpattern.agent.react;

import com.example.agentpattern.agent.core.Agent;
import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.agent.orchestrator.core.Orchestrator;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorRegistry;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ReAct Agent实现
 * 使用ReActOrchestrator编排器的Agent包装类
 * 保持向后兼容性
 */
@Slf4j
@Component
public class ReactAgent implements Agent {

    private final OrchestratorRegistry orchestratorRegistry;

    public ReactAgent(OrchestratorRegistry orchestratorRegistry) {
        this.orchestratorRegistry = orchestratorRegistry;
    }

    @Override
    public String getName() {
        return "ReActAgent";
    }

    @Override
    public String getDescription() {
        return "A ReAct (Reasoning and Acting) Agent that thinks and acts iteratively";
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
            // 获取ReAct编排器
            Orchestrator orchestrator = orchestratorRegistry.getOrchestrator("react")
                    .orElseThrow(() -> new IllegalStateException("ReAct orchestrator not found"));

            log.debug("Delegating to ReAct orchestrator");

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
            log.error("Error executing ReAct Agent", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return AgentResponse.failure("Error: " + e.getMessage(), context, executionTime);
        }
    }
}
