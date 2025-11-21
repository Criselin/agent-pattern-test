package com.example.agentpattern.agent.orchestrator.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 编排器注册表
 * 管理所有可用的编排器
 */
@Slf4j
@Component
public class OrchestratorRegistry {

    private final Map<String, Orchestrator> orchestrators = new ConcurrentHashMap<>();
    private String defaultOrchestratorName;

    /**
     * 注册编排器
     */
    public void registerOrchestrator(Orchestrator orchestrator) {
        if (orchestrator == null || orchestrator.getName() == null) {
            throw new IllegalArgumentException("Orchestrator and name cannot be null");
        }
        orchestrators.put(orchestrator.getName(), orchestrator);
        log.info("Registered orchestrator: {} (type: {})",
                orchestrator.getName(),
                orchestrator.getType().getDisplayName());

        // 如果是第一个注册的，设为默认
        if (defaultOrchestratorName == null) {
            defaultOrchestratorName = orchestrator.getName();
            log.info("Set default orchestrator: {}", orchestrator.getName());
        }
    }

    /**
     * 获取编排器
     */
    public Optional<Orchestrator> getOrchestrator(String name) {
        return Optional.ofNullable(orchestrators.get(name));
    }

    /**
     * 获取默认编排器
     */
    public Optional<Orchestrator> getDefaultOrchestrator() {
        return Optional.ofNullable(orchestrators.get(defaultOrchestratorName));
    }

    /**
     * 设置默认编排器
     */
    public void setDefaultOrchestrator(String name) {
        if (orchestrators.containsKey(name)) {
            defaultOrchestratorName = name;
            log.info("Set default orchestrator: {}", name);
        } else {
            throw new IllegalArgumentException("Orchestrator not found: " + name);
        }
    }

    /**
     * 获取所有编排器
     */
    public Collection<Orchestrator> getAllOrchestrators() {
        return Collections.unmodifiableCollection(orchestrators.values());
    }

    /**
     * 获取所有编排器名称
     */
    public Set<String> getOrchestratorNames() {
        return Collections.unmodifiableSet(orchestrators.keySet());
    }

    /**
     * 移除编排器
     */
    public void unregisterOrchestrator(String name) {
        orchestrators.remove(name);
        log.info("Unregistered orchestrator: {}", name);
    }

    /**
     * 获取编排器数量
     */
    public int size() {
        return orchestrators.size();
    }

    /**
     * 生成编排器列表描述
     */
    public String getOrchestratorsDescription() {
        if (orchestrators.isEmpty()) {
            return "No orchestrators available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available orchestrators:\n");
        for (Orchestrator orc : orchestrators.values()) {
            sb.append("- ").append(orc.getName())
                    .append(" (").append(orc.getType().getDisplayName()).append(")")
                    .append(": ").append(orc.getDescription());
            if (orc.getName().equals(defaultOrchestratorName)) {
                sb.append(" [DEFAULT]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
