package com.example.agentpattern.knowledge.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识库注册表
 * 管理所有可用的知识库
 */
@Slf4j
@Component
public class KnowledgeBaseRegistry {

    private final Map<String, KnowledgeBase> knowledgeBases = new ConcurrentHashMap<>();

    /**
     * 注册知识库
     */
    public void registerKnowledgeBase(KnowledgeBase knowledgeBase) {
        if (knowledgeBase == null || knowledgeBase.getName() == null) {
            throw new IllegalArgumentException("KnowledgeBase and name cannot be null");
        }
        knowledgeBases.put(knowledgeBase.getName(), knowledgeBase);
        log.info("Registered knowledge base: {} (type: {}, documents: {})",
                knowledgeBase.getName(),
                knowledgeBase.getType(),
                knowledgeBase.getDocumentCount());
    }

    /**
     * 获取知识库
     */
    public Optional<KnowledgeBase> getKnowledgeBase(String name) {
        return Optional.ofNullable(knowledgeBases.get(name));
    }

    /**
     * 获取所有知识库
     */
    public Collection<KnowledgeBase> getAllKnowledgeBases() {
        return Collections.unmodifiableCollection(knowledgeBases.values());
    }

    /**
     * 获取所有知识库名称
     */
    public Set<String> getKnowledgeBaseNames() {
        return Collections.unmodifiableSet(knowledgeBases.keySet());
    }

    /**
     * 移除知识库
     */
    public void unregisterKnowledgeBase(String name) {
        knowledgeBases.remove(name);
        log.info("Unregistered knowledge base: {}", name);
    }

    /**
     * 获取知识库数量
     */
    public int size() {
        return knowledgeBases.size();
    }

    /**
     * 生成知识库列表描述（用于提示词）
     */
    public String getKnowledgeBasesDescription() {
        if (knowledgeBases.isEmpty()) {
            return "No knowledge bases available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available knowledge bases:\n");
        for (KnowledgeBase kb : knowledgeBases.values()) {
            sb.append("- ").append(kb.getName())
                    .append(" (").append(kb.getType()).append(")")
                    .append(": ").append(kb.getDescription())
                    .append(" [").append(kb.getDocumentCount()).append(" documents]")
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 搜索所有知识库
     *
     * @param query 查询文本
     * @param topK 每个知识库返回的结果数
     * @return 所有知识库的搜索结果
     */
    public Map<String, SearchResult> searchAll(String query, int topK) {
        Map<String, SearchResult> results = new HashMap<>();
        for (KnowledgeBase kb : knowledgeBases.values()) {
            try {
                SearchResult result = kb.search(query, topK);
                results.put(kb.getName(), result);
            } catch (Exception e) {
                log.error("Error searching knowledge base: {}", kb.getName(), e);
            }
        }
        return results;
    }
}
