package com.example.agentpattern.agent.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent工具接口
 * 定义了工具的基本行为和元数据
 */
public interface Tool {

    /**
     * 获取工具名称
     */
    String getName();

    /**
     * 获取工具描述
     */
    String getDescription();

    /**
     * 执行工具
     *
     * @param input 工具输入参数
     * @return 工具执行结果
     */
    ToolResult execute(String input);

    /**
     * 获取工具参数schema（JSON Schema格式）
     */
    default String getParameterSchema() {
        return "{}";
    }

    /**
     * 工具执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ToolResult {
        @JsonProperty("success")
        private boolean success;

        @JsonProperty("output")
        private String output;

        @JsonProperty("error")
        private String error;

        public static ToolResult success(String output) {
            return ToolResult.builder()
                    .success(true)
                    .output(output)
                    .build();
        }

        public static ToolResult failure(String error) {
            return ToolResult.builder()
                    .success(false)
                    .error(error)
                    .build();
        }
    }
}
