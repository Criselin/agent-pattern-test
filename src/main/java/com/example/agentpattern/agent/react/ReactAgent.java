package com.example.agentpattern.agent.react;

import com.example.agentpattern.agent.core.Agent;
import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct Agent实现
 * 实现了Reasoning and Acting的Agent模式
 */
@Slf4j
@Component
public class ReactAgent implements Agent {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;

    // 正则表达式用于解析LLM输出
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("Thought:\\s*(.+?)(?=\\n(?:Action:|Final Answer:|$))", Pattern.DOTALL);
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(.+?)(?=\\n)", Pattern.DOTALL);
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile("Action Input:\\s*(.+?)(?=\\n|$)", Pattern.DOTALL);
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile("Final Answer:\\s*(.+)", Pattern.DOTALL);

    public ReactAgent(ChatModel chatModel, ToolRegistry toolRegistry) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
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
            log.debug("Starting ReAct Agent execution for input: {}", context.getInput());

            // 构建系统提示词
            String systemPrompt = ReactPromptTemplate.buildSystemPrompt(toolRegistry);

            // ReAct循环
            while (!context.hasReachedMaxIterations()) {
                context.incrementIteration();

                // 构建用户提示词（包含历史步骤）
                String scratchpad = ReactPromptTemplate.buildScratchpad(context);
                String userPrompt = ReactPromptTemplate.buildUserPrompt(context.getInput(), scratchpad);

                log.debug("Iteration {}: Sending prompt to LLM", context.getCurrentIteration());

                // 调用LLM
                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPrompt)
                ));

                String llmResponse = chatModel.call(prompt).getResult().getOutput().getContent();
                log.debug("LLM Response: {}", llmResponse);

                // 检查是否有最终答案
                String finalAnswer = extractFinalAnswer(llmResponse);
                if (finalAnswer != null) {
                    log.info("ReAct Agent completed successfully in {} iterations", context.getCurrentIteration());
                    long executionTime = System.currentTimeMillis() - startTime;
                    return AgentResponse.success(finalAnswer, context, executionTime);
                }

                // 解析思考、动作和动作输入
                String thought = extractThought(llmResponse);
                String action = extractAction(llmResponse);
                String actionInput = extractActionInput(llmResponse);

                if (action == null) {
                    log.warn("No action found in LLM response, stopping");
                    long executionTime = System.currentTimeMillis() - startTime;
                    return AgentResponse.failure("Unable to determine action from LLM response", context, executionTime);
                }

                // 执行工具
                String observation = executeTool(action, actionInput);

                // 记录步骤
                AgentContext.AgentStep step = AgentContext.AgentStep.builder()
                        .thought(thought)
                        .action(action)
                        .actionInput(actionInput)
                        .observation(observation)
                        .build();
                context.addStep(step);

                log.debug("Completed iteration {}: Action={}, Observation={}",
                         context.getCurrentIteration(), action, observation);
            }

            // 达到最大迭代次数
            log.warn("Reached maximum iterations without finding final answer");
            long executionTime = System.currentTimeMillis() - startTime;
            return AgentResponse.failure(
                    "Agent reached maximum iterations without finding a final answer",
                    context,
                    executionTime
            );

        } catch (Exception e) {
            log.error("Error executing ReAct Agent", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return AgentResponse.failure("Error: " + e.getMessage(), context, executionTime);
        }
    }

    private String extractThought(String text) {
        Matcher matcher = THOUGHT_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String extractAction(String text) {
        Matcher matcher = ACTION_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String extractActionInput(String text) {
        Matcher matcher = ACTION_INPUT_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private String extractFinalAnswer(String text) {
        Matcher matcher = FINAL_ANSWER_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

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
}
