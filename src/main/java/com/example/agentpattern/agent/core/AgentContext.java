package com.example.agentpattern.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent执行上下文
 * 保存Agent执行过程中的状态和历史信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {

    /**
     * 用户输入
     */
    private String input;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 中间步骤历史
     */
    @Builder.Default
    private List<AgentStep> steps = new ArrayList<>();

    /**
     * 上下文变量
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 最大迭代次数
     */
    @Builder.Default
    private int maxIterations = 5;

    /**
     * 当前迭代次数
     */
    @Builder.Default
    private int currentIteration = 0;

    /**
     * 添加步骤
     */
    public void addStep(AgentStep step) {
        steps.add(step);
    }

    /**
     * 设置变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 获取变量
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 增加迭代次数
     */
    public void incrementIteration() {
        currentIteration++;
    }

    /**
     * 是否达到最大迭代次数
     */
    public boolean hasReachedMaxIterations() {
        return currentIteration >= maxIterations;
    }

    /**
     * Agent执行步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStep {
        /**
         * 思考内容
         */
        private String thought;

        /**
         * 动作（工具名称）
         */
        private String action;

        /**
         * 动作输入
         */
        private String actionInput;

        /**
         * 观察结果
         */
        private String observation;

        /**
         * 时间戳
         */
        @Builder.Default
        private long timestamp = System.currentTimeMillis();
    }
}
