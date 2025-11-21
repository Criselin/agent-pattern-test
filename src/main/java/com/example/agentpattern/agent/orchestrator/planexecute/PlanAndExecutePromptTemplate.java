package com.example.agentpattern.agent.orchestrator.planexecute;

import com.example.agentpattern.agent.tool.ToolRegistry;

/**
 * Plan and Execute提示词模板
 */
public class PlanAndExecutePromptTemplate {

    private static final String PLANNER_SYSTEM_PROMPT = """
            You are a strategic planner that creates step-by-step plans to solve user questions.

            Available tools:
            {tools}

            Your task is to:
            1. Analyze the user's question carefully
            2. Break it down into logical steps
            3. For each step, specify which tool to use and what input to provide
            4. Create a clear, sequential plan

            Output format:
            Plan: [Brief description of the overall plan]

            Step 1: [Description of step 1]
            Tool: [tool-name]
            Input: [tool input]

            Step 2: [Description of step 2]
            Tool: [tool-name]
            Input: [tool input]

            ... (continue for all steps)

            Important guidelines:
            - Keep each step focused on a single action
            - Use tool names exactly as provided
            - Make steps depend on previous results when needed
            - If no tools are needed, use tool name as "none"
            - Be concise and clear
            """;

    private static final String PLANNER_USER_PROMPT = """
            Question: {input}

            Please create a step-by-step plan to answer this question.
            """;

    private static final String EXECUTOR_SYSTEM_PROMPT = """
            You are an executor that synthesizes information to answer user questions.

            You will be given:
            1. The original question
            2. A plan that was executed
            3. Results from each step

            Your task is to:
            - Analyze all the step results
            - Synthesize the information
            - Provide a comprehensive, well-structured answer to the original question

            Guidelines:
            - Be clear and concise
            - Use information from the step results
            - If information is incomplete, acknowledge it
            - Format your answer in a user-friendly way
            """;

    private static final String EXECUTOR_USER_PROMPT = """
            Original Question: {input}

            Executed Plan:
            {plan_execution}

            Based on the plan execution results above, please provide a comprehensive answer to the original question.
            """;

    /**
     * 构建计划器的系统提示词
     */
    public static String buildPlannerSystemPrompt(ToolRegistry toolRegistry) {
        String tools = toolRegistry.getToolsDescription();
        return PLANNER_SYSTEM_PROMPT.replace("{tools}", tools);
    }

    /**
     * 构建计划器的用户提示词
     */
    public static String buildPlannerUserPrompt(String input) {
        return PLANNER_USER_PROMPT.replace("{input}", input);
    }

    /**
     * 构建执行器的系统提示词
     */
    public static String buildExecutorSystemPrompt() {
        return EXECUTOR_SYSTEM_PROMPT;
    }

    /**
     * 构建执行器的用户提示词
     */
    public static String buildExecutorUserPrompt(String input, String planExecution) {
        return EXECUTOR_USER_PROMPT
                .replace("{input}", input)
                .replace("{plan_execution}", planExecution);
    }

    /**
     * 格式化计划执行结果
     */
    public static String formatPlanExecution(Plan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("Plan: ").append(plan.getDescription()).append("\n\n");

        for (Plan.PlanStep step : plan.getSteps()) {
            sb.append("Step ").append(step.getStepNumber()).append(": ")
                    .append(step.getDescription()).append("\n");
            sb.append("Tool: ").append(step.getTool()).append("\n");
            sb.append("Input: ").append(step.getToolInput()).append("\n");
            sb.append("Result: ").append(step.getResult() != null ? step.getResult() : "Not executed").append("\n");
            sb.append("Status: ").append(step.getStatus()).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }
}
