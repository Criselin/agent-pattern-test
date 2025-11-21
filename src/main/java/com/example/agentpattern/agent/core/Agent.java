package com.example.agentpattern.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent接口
 * 定义了Agent的核心行为
 */
public interface Agent {

    /**
     * 执行Agent任务
     *
     * @param input 用户输入
     * @return Agent响应
     */
    AgentResponse execute(String input);

    /**
     * 执行Agent任务（带上下文）
     *
     * @param context Agent上下文
     * @return Agent响应
     */
    AgentResponse execute(AgentContext context);

    /**
     * 获取Agent名称
     */
    String getName();

    /**
     * 获取Agent描述
     */
    String getDescription();

    /**
     * Agent响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AgentResponse {
        /**
         * 最终答案
         */
        private String answer;

        /**
         * 执行上下文
         */
        private AgentContext context;

        /**
         * 是否成功
         */
        @Builder.Default
        private boolean success = true;

        /**
         * 错误信息
         */
        private String error;

        /**
         * 执行时间（毫秒）
         */
        private long executionTime;

        public static AgentResponse success(String answer, AgentContext context, long executionTime) {
            return AgentResponse.builder()
                    .answer(answer)
                    .context(context)
                    .success(true)
                    .executionTime(executionTime)
                    .build();
        }

        public static AgentResponse failure(String error, AgentContext context, long executionTime) {
            return AgentResponse.builder()
                    .error(error)
                    .context(context)
                    .success(false)
                    .executionTime(executionTime)
                    .build();
        }
    }
}
