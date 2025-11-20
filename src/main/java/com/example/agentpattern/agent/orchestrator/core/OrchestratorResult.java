package com.example.agentpattern.agent.orchestrator.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编排器执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorResult {

    /**
     * 最终答案
     */
    private String answer;

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
     * 执行的步骤数
     */
    private int stepCount;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTimeMs;

    /**
     * 使用的编排器类型
     */
    private String orchestratorType;

    public static OrchestratorResult success(String answer, int stepCount, long executionTimeMs, String orchestratorType) {
        return OrchestratorResult.builder()
                .answer(answer)
                .success(true)
                .stepCount(stepCount)
                .executionTimeMs(executionTimeMs)
                .orchestratorType(orchestratorType)
                .build();
    }

    public static OrchestratorResult failure(String error, int stepCount, long executionTimeMs, String orchestratorType) {
        return OrchestratorResult.builder()
                .error(error)
                .success(false)
                .stepCount(stepCount)
                .executionTimeMs(executionTimeMs)
                .orchestratorType(orchestratorType)
                .build();
    }
}
