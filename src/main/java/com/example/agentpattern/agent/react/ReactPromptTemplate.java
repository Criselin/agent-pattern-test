package com.example.agentpattern.agent.react;

import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.agent.tool.ToolRegistry;

/**
 * ReAct提示词模板
 * 用于生成ReAct Agent的提示词
 */
public class ReactPromptTemplate {

    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that uses the ReAct (Reasoning and Acting) approach to solve problems.

            You have access to the following tools:
            {tools}

            Use the following format:

            Question: the input question you must answer
            Thought: you should always think about what to do
            Action: the action to take, should be one of [{tool_names}]
            Action Input: the input to the action
            Observation: the result of the action
            ... (this Thought/Action/Action Input/Observation can repeat N times)
            Thought: I now know the final answer
            Final Answer: the final answer to the original input question

            Important:
            - Always follow the format strictly
            - Use one tool at a time
            - Think step by step
            - When you have enough information, provide the Final Answer

            Begin!
            """;

    private static final String USER_PROMPT = """
            Question: {input}
            {scratchpad}
            """;

    public static String buildSystemPrompt(ToolRegistry toolRegistry) {
        String tools = toolRegistry.getToolsDescription();
        String toolNames = String.join(", ", toolRegistry.getToolNames());

        return SYSTEM_PROMPT
                .replace("{tools}", tools)
                .replace("{tool_names}", toolNames);
    }

    public static String buildUserPrompt(String input, String scratchpad) {
        return USER_PROMPT
                .replace("{input}", input)
                .replace("{scratchpad}", scratchpad);
    }

    /**
     * 从上下文构建scratchpad（思考过程记录）
     */
    public static String buildScratchpad(AgentContext context) {
        if (context.getSteps().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (AgentContext.AgentStep step : context.getSteps()) {
            if (step.getThought() != null) {
                sb.append("Thought: ").append(step.getThought()).append("\n");
            }
            if (step.getAction() != null) {
                sb.append("Action: ").append(step.getAction()).append("\n");
            }
            if (step.getActionInput() != null) {
                sb.append("Action Input: ").append(step.getActionInput()).append("\n");
            }
            if (step.getObservation() != null) {
                sb.append("Observation: ").append(step.getObservation()).append("\n");
            }
        }
        return sb.toString();
    }
}
