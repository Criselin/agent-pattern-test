package com.example.agentpattern.agent.orchestrator.planexecute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行计划模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    /**
     * 计划描述
     */
    private String description;

    /**
     * 计划步骤
     */
    @Builder.Default
    private List<PlanStep> steps = new ArrayList<>();

    /**
     * 计划状态
     */
    @Builder.Default
    private PlanStatus status = PlanStatus.PENDING;

    /**
     * 添加步骤
     */
    public void addStep(PlanStep step) {
        steps.add(step);
    }

    /**
     * 获取当前步骤（第一个未完成的步骤）
     */
    public PlanStep getCurrentStep() {
        return steps.stream()
                .filter(step -> step.getStatus() != PlanStep.StepStatus.COMPLETED)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查计划是否完成
     */
    public boolean isCompleted() {
        return steps.stream().allMatch(step -> step.getStatus() == PlanStep.StepStatus.COMPLETED);
    }

    /**
     * 获取已完成的步骤数
     */
    public int getCompletedStepCount() {
        return (int) steps.stream()
                .filter(step -> step.getStatus() == PlanStep.StepStatus.COMPLETED)
                .count();
    }

    /**
     * 计划状态
     */
    public enum PlanStatus {
        PENDING,    // 待执行
        EXECUTING,  // 执行中
        COMPLETED,  // 已完成
        FAILED      // 失败
    }

    /**
     * 计划步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanStep {
        /**
         * 步骤编号
         */
        private int stepNumber;

        /**
         * 步骤描述
         */
        private String description;

        /**
         * 需要使用的工具
         */
        private String tool;

        /**
         * 工具输入
         */
        private String toolInput;

        /**
         * 执行结果
         */
        private String result;

        /**
         * 步骤状态
         */
        @Builder.Default
        private StepStatus status = StepStatus.PENDING;

        /**
         * 依赖的步骤（可选）
         */
        private List<Integer> dependencies;

        /**
         * 步骤状态
         */
        public enum StepStatus {
            PENDING,      // 待执行
            EXECUTING,    // 执行中
            COMPLETED,    // 已完成
            FAILED,       // 失败
            SKIPPED       // 跳过
        }
    }
}
