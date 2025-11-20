package com.example.agentpattern.agent.orchestrator.core;

import com.example.agentpattern.agent.core.AgentContext;

/**
 * 编排器接口
 * 定义Agent的执行编排策略
 */
public interface Orchestrator {

    /**
     * 获取编排器名称
     */
    String getName();

    /**
     * 获取编排器描述
     */
    String getDescription();

    /**
     * 获取编排器类型
     */
    OrchestratorType getType();

    /**
     * 执行编排
     *
     * @param context Agent上下文
     * @return 编排结果（最终答案）
     */
    OrchestratorResult orchestrate(AgentContext context);

    /**
     * 是否支持流式输出
     */
    default boolean supportsStreaming() {
        return false;
    }

    /**
     * 编排器类型枚举
     */
    enum OrchestratorType {
        REACT("ReAct", "Reasoning and Acting循环模式"),
        PLAN_EXECUTE("Plan and Execute", "先计划后执行模式"),
        SELF_ASK("Self-Ask", "自问自答模式"),
        TREE_OF_THOUGHT("Tree of Thought", "思维树模式"),
        CUSTOM("Custom", "自定义模式");

        private final String displayName;
        private final String description;

        OrchestratorType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}
