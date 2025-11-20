package com.example.agentpattern.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表
 * 管理所有可用的工具
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /**
     * 注册工具
     */
    public void registerTool(Tool tool) {
        if (tool == null || tool.getName() == null) {
            throw new IllegalArgumentException("Tool and tool name cannot be null");
        }
        tools.put(tool.getName(), tool);
        log.info("Registered tool: {}", tool.getName());
    }

    /**
     * 获取工具
     */
    public Optional<Tool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * 获取所有工具
     */
    public Collection<Tool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    /**
     * 获取所有工具名称
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    /**
     * 移除工具
     */
    public void unregisterTool(String name) {
        tools.remove(name);
        log.info("Unregistered tool: {}", name);
    }

    /**
     * 获取工具数量
     */
    public int size() {
        return tools.size();
    }

    /**
     * 生成工具列表描述（用于提示词）
     */
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available tools:\n");
        for (Tool tool : tools.values()) {
            sb.append("- ").append(tool.getName())
                    .append(": ").append(tool.getDescription())
                    .append("\n");
        }
        return sb.toString();
    }
}
