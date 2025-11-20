package com.example.agentpattern.agent.orchestrator.planexecute;

import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.agent.orchestrator.core.Orchestrator;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorResult;
import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plan and Execute编排器
 * 实现先计划后执行的编排模式
 */
@Slf4j
@Component
public class PlanAndExecuteOrchestrator implements Orchestrator {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;

    // 正则表达式用于解析计划
    private static final Pattern PLAN_DESCRIPTION_PATTERN = Pattern.compile("Plan:\\s*(.+?)(?=\\n\\n|Step)", Pattern.DOTALL);
    private static final Pattern STEP_PATTERN = Pattern.compile(
            "Step\\s+(\\d+):\\s*(.+?)\\s*Tool:\\s*(.+?)\\s*Input:\\s*(.+?)(?=\\n\\n|Step|$)",
            Pattern.DOTALL
    );

    public PlanAndExecuteOrchestrator(ChatModel chatModel, ToolRegistry toolRegistry) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public String getName() {
        return "plan-execute";
    }

    @Override
    public String getDescription() {
        return "先计划后执行模式，首先制定完整计划，然后按步骤执行";
    }

    @Override
    public OrchestratorType getType() {
        return OrchestratorType.PLAN_EXECUTE;
    }

    @Override
    public OrchestratorResult orchestrate(AgentContext context) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Starting Plan and Execute orchestration for input: {}", context.getInput());

            // 阶段1: 制定计划
            Plan plan = createPlan(context);
            if (plan == null || plan.getSteps().isEmpty()) {
                log.error("Failed to create a valid plan");
                long executionTime = System.currentTimeMillis() - startTime;
                return OrchestratorResult.failure(
                        "Failed to create a valid execution plan",
                        0,
                        executionTime,
                        getName()
                );
            }

            log.info("Created plan with {} steps", plan.getSteps().size());
            plan.setStatus(Plan.PlanStatus.EXECUTING);

            // 阶段2: 执行计划
            boolean success = executePlan(plan, context);

            if (!success) {
                log.error("Plan execution failed");
                long executionTime = System.currentTimeMillis() - startTime;
                return OrchestratorResult.failure(
                        "Plan execution failed",
                        plan.getSteps().size(),
                        executionTime,
                        getName()
                );
            }

            plan.setStatus(Plan.PlanStatus.COMPLETED);
            log.info("Plan execution completed successfully");

            // 阶段3: 综合结果生成最终答案
            String finalAnswer = synthesizeAnswer(context, plan);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Plan and Execute orchestration completed in {}ms", executionTime);

            return OrchestratorResult.success(
                    finalAnswer,
                    plan.getSteps().size(),
                    executionTime,
                    getName()
            );

        } catch (Exception e) {
            log.error("Error in Plan and Execute orchestration", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return OrchestratorResult.failure(
                    "Error: " + e.getMessage(),
                    0,
                    executionTime,
                    getName()
            );
        }
    }

    /**
     * 阶段1: 创建执行计划
     */
    private Plan createPlan(AgentContext context) {
        try {
            log.debug("Creating execution plan...");

            String systemPrompt = PlanAndExecutePromptTemplate.buildPlannerSystemPrompt(toolRegistry);
            String userPrompt = PlanAndExecutePromptTemplate.buildPlannerUserPrompt(context.getInput());

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));

            String llmResponse = chatModel.call(prompt).getResult().getOutput().getContent();
            log.debug("Plan from LLM: {}", llmResponse);

            return parsePlan(llmResponse);

        } catch (Exception e) {
            log.error("Error creating plan", e);
            return null;
        }
    }

    /**
     * 解析LLM生成的计划
     */
    private Plan parsePlan(String text) {
        Plan plan = new Plan();

        // 提取计划描述
        Matcher descMatcher = PLAN_DESCRIPTION_PATTERN.matcher(text);
        if (descMatcher.find()) {
            plan.setDescription(descMatcher.group(1).trim());
        } else {
            plan.setDescription("Execution plan");
        }

        // 提取步骤
        Matcher stepMatcher = STEP_PATTERN.matcher(text);
        while (stepMatcher.find()) {
            int stepNumber = Integer.parseInt(stepMatcher.group(1).trim());
            String description = stepMatcher.group(2).trim();
            String tool = stepMatcher.group(3).trim();
            String input = stepMatcher.group(4).trim();

            Plan.PlanStep step = Plan.PlanStep.builder()
                    .stepNumber(stepNumber)
                    .description(description)
                    .tool(tool)
                    .toolInput(input)
                    .status(Plan.PlanStep.StepStatus.PENDING)
                    .build();

            plan.addStep(step);
        }

        return plan;
    }

    /**
     * 阶段2: 执行计划
     */
    private boolean executePlan(Plan plan, AgentContext context) {
        try {
            log.info("Executing plan with {} steps", plan.getSteps().size());

            for (Plan.PlanStep step : plan.getSteps()) {
                log.debug("Executing step {}: {}", step.getStepNumber(), step.getDescription());
                step.setStatus(Plan.PlanStep.StepStatus.EXECUTING);

                String result;
                if ("none".equalsIgnoreCase(step.getTool())) {
                    // 不需要工具的步骤
                    result = "No tool execution needed for this step";
                } else {
                    // 执行工具
                    result = executeTool(step.getTool(), step.getToolInput());
                }

                step.setResult(result);
                step.setStatus(Plan.PlanStep.StepStatus.COMPLETED);

                // 记录到Agent上下文
                AgentContext.AgentStep agentStep = AgentContext.AgentStep.builder()
                        .thought("Executing plan step " + step.getStepNumber() + ": " + step.getDescription())
                        .action(step.getTool())
                        .actionInput(step.getToolInput())
                        .observation(result)
                        .build();
                context.addStep(agentStep);

                log.debug("Step {} completed: {}", step.getStepNumber(), result);
            }

            return true;

        } catch (Exception e) {
            log.error("Error executing plan", e);
            return false;
        }
    }

    /**
     * 执行工具
     */
    private String executeTool(String toolName, String input) {
        Tool tool = toolRegistry.getTool(toolName).orElse(null);

        if (tool == null) {
            String error = "Tool not found: " + toolName + ". Available tools: " +
                    String.join(", ", toolRegistry.getToolNames());
            log.warn(error);
            return error;
        }

        try {
            Tool.ToolResult result = tool.execute(input);
            return result.isSuccess() ? result.getOutput() : "Error: " + result.getError();
        } catch (Exception e) {
            String error = "Error executing tool " + toolName + ": " + e.getMessage();
            log.error(error, e);
            return error;
        }
    }

    /**
     * 阶段3: 综合结果生成最终答案
     */
    private String synthesizeAnswer(AgentContext context, Plan plan) {
        try {
            log.debug("Synthesizing final answer...");

            String systemPrompt = PlanAndExecutePromptTemplate.buildExecutorSystemPrompt();
            String planExecution = PlanAndExecutePromptTemplate.formatPlanExecution(plan);
            String userPrompt = PlanAndExecutePromptTemplate.buildExecutorUserPrompt(
                    context.getInput(),
                    planExecution
            );

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));

            String finalAnswer = chatModel.call(prompt).getResult().getOutput().getContent();
            log.debug("Final answer synthesized");

            return finalAnswer;

        } catch (Exception e) {
            log.error("Error synthesizing answer", e);
            // 如果综合失败，返回步骤结果的简单拼接
            StringBuilder sb = new StringBuilder();
            sb.append("Based on the plan execution:\n\n");
            for (Plan.PlanStep step : plan.getSteps()) {
                sb.append("Step ").append(step.getStepNumber()).append(": ")
                        .append(step.getDescription()).append("\n");
                sb.append("Result: ").append(step.getResult()).append("\n\n");
            }
            return sb.toString();
        }
    }
}
