package com.example.agentpattern.agent.orchestrator.react;

import com.example.agentpattern.agent.core.AgentContext;
import com.example.agentpattern.agent.orchestrator.core.Orchestrator;
import com.example.agentpattern.agent.orchestrator.core.OrchestratorResult;
import com.example.agentpattern.agent.react.ReactPromptTemplate;
import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import jakarta.annotation.PostConstruct;
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
 * ReAct编排器
 * 实现Reasoning and Acting循环编排模式
 */
@Slf4j
@Component
public class ReActOrchestrator implements Orchestrator {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;

    // 正则表达式用于解析LLM输出
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("Thought:\\s*(.+?)(?=\\n(?:Action:|Final Answer:|$))", Pattern.DOTALL);
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(.+?)(?=\\n)", Pattern.DOTALL);
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile("Action Input:\\s*(.+?)(?=\\n|$)", Pattern.DOTALL);
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile("Final Answer:\\s*(.+)", Pattern.DOTALL);

    public ReActOrchestrator(ChatModel chatModel, ToolRegistry toolRegistry) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public String getName() {
        return "react";
    }

    @Override
    public String getDescription() {
        return "Reasoning and Acting循环模式，通过思考-行动-观察的迭代来解决问题";
    }

    @Override
    public OrchestratorType getType() {
        return OrchestratorType.REACT;
    }

    @Override
    public OrchestratorResult orchestrate(AgentContext context) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("Starting ReAct orchestration for input: {}", context.getInput());

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
                    log.info("ReAct orchestration completed successfully in {} iterations", context.getCurrentIteration());
                    long executionTime = System.currentTimeMillis() - startTime;
                    return OrchestratorResult.success(
                            finalAnswer,
                            context.getSteps().size(),
                            executionTime,
                            getName()
                    );
                }

                // 解析思考、动作和动作输入
                String thought = extractThought(llmResponse);
                String action = extractAction(llmResponse);
                String actionInput = extractActionInput(llmResponse);

                if (action == null) {
                    log.warn("No action found in LLM response, stopping");
                    long executionTime = System.currentTimeMillis() - startTime;
                    return OrchestratorResult.failure(
                            "Unable to determine action from LLM response",
                            context.getSteps().size(),
                            executionTime,
                            getName()
                    );
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
            return OrchestratorResult.failure(
                    "Reached maximum iterations without finding a final answer",
                    context.getSteps().size(),
                    executionTime,
                    getName()
            );

        } catch (Exception e) {
            log.error("Error in ReAct orchestration", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return OrchestratorResult.failure(
                    "Error: " + e.getMessage(),
                    context.getSteps().size(),
                    executionTime,
                    getName()
            );
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
