package com.example.agentpattern.tools;

import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import com.example.agentpattern.knowledge.base.KnowledgeBase;
import com.example.agentpattern.knowledge.base.KnowledgeBaseRegistry;
import com.example.agentpattern.knowledge.base.SearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 知识库搜索工具
 * 允许Agent动态调用知识库进行信息检索
 */
@Slf4j
@Component
public class KnowledgeSearchTool implements Tool {

    private final ToolRegistry toolRegistry;
    private final KnowledgeBaseRegistry knowledgeBaseRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KnowledgeSearchTool(ToolRegistry toolRegistry, KnowledgeBaseRegistry knowledgeBaseRegistry) {
        this.toolRegistry = toolRegistry;
        this.knowledgeBaseRegistry = knowledgeBaseRegistry;
    }

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
        log.info("KnowledgeSearchTool registered");
    }

    @Override
    public String getName() {
        return "knowledge-search";
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Search the knowledge base for relevant information. ");
        desc.append("Input should be a JSON object with 'query' (required) and optionally 'knowledge_base' (string) and 'top_k' (number). ");
        desc.append("If knowledge_base is not specified, searches all available knowledge bases. ");

        // 列出可用的知识库
        if (knowledgeBaseRegistry.size() > 0) {
            desc.append("Available knowledge bases: ");
            desc.append(String.join(", ", knowledgeBaseRegistry.getKnowledgeBaseNames()));
        }

        return desc.toString();
    }

    @Override
    public ToolResult execute(String input) {
        try {
            log.debug("Executing knowledge search with input: {}", input);

            // 解析输入
            SearchRequest request = parseInput(input);

            // 执行搜索
            if (request.knowledgeBase != null && !request.knowledgeBase.isEmpty()) {
                // 搜索指定的知识库
                return searchSpecificKnowledgeBase(request);
            } else {
                // 搜索所有知识库
                return searchAllKnowledgeBases(request);
            }

        } catch (Exception e) {
            log.error("Error executing knowledge search", e);
            return ToolResult.failure("知识库搜索失败: " + e.getMessage());
        }
    }

    @Override
    public String getParameterSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "query": {
                      "type": "string",
                      "description": "搜索查询文本"
                    },
                    "knowledge_base": {
                      "type": "string",
                      "description": "要搜索的知识库名称（可选）"
                    },
                    "top_k": {
                      "type": "number",
                      "description": "返回的结果数量（默认3）"
                    }
                  },
                  "required": ["query"]
                }
                """;
    }

    /**
     * 解析输入参数
     */
    private SearchRequest parseInput(String input) {
        try {
            // 尝试解析JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> params = objectMapper.readValue(input, Map.class);

            String query = (String) params.get("query");
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Query cannot be empty");
            }

            String knowledgeBase = (String) params.get("knowledge_base");
            Integer topK = params.containsKey("top_k") ?
                    ((Number) params.get("top_k")).intValue() : 3;

            return new SearchRequest(query.trim(), knowledgeBase, topK);

        } catch (Exception e) {
            // 如果不是JSON，当作纯文本查询
            log.debug("Input is not JSON, treating as plain text query");
            return new SearchRequest(input.trim(), null, 3);
        }
    }

    /**
     * 搜索指定的知识库
     */
    private ToolResult searchSpecificKnowledgeBase(SearchRequest request) {
        KnowledgeBase kb = knowledgeBaseRegistry.getKnowledgeBase(request.knowledgeBase)
                .orElse(null);

        if (kb == null) {
            String availableKbs = String.join(", ", knowledgeBaseRegistry.getKnowledgeBaseNames());
            return ToolResult.failure(
                    String.format("知识库 '%s' 不存在。可用的知识库: %s",
                            request.knowledgeBase, availableKbs)
            );
        }

        SearchResult result = kb.search(request.query, request.topK);

        if (result.getDocuments().isEmpty()) {
            return ToolResult.success(
                    String.format("在知识库 '%s' 中未找到与 '%s' 相关的信息。",
                            request.knowledgeBase, request.query)
            );
        }

        String formattedResult = String.format(
                "从知识库 '%s' 检索到的信息:\n\n%s",
                request.knowledgeBase,
                result.formatForLLM()
        );

        return ToolResult.success(formattedResult);
    }

    /**
     * 搜索所有知识库
     */
    private ToolResult searchAllKnowledgeBases(SearchRequest request) {
        if (knowledgeBaseRegistry.size() == 0) {
            return ToolResult.failure("没有可用的知识库。");
        }

        Map<String, SearchResult> results = knowledgeBaseRegistry.searchAll(request.query, request.topK);

        // 合并所有结果
        StringBuilder output = new StringBuilder();
        output.append(String.format("搜索所有知识库，查询: '%s'\n\n", request.query));

        int totalResults = 0;
        for (Map.Entry<String, SearchResult> entry : results.entrySet()) {
            SearchResult result = entry.getValue();
            if (!result.getDocuments().isEmpty()) {
                output.append(String.format("=== 来自知识库: %s ===\n", entry.getKey()));
                output.append(result.formatForLLM());
                output.append("\n");
                totalResults += result.getDocuments().size();
            }
        }

        if (totalResults == 0) {
            return ToolResult.success(
                    String.format("在所有知识库中均未找到与 '%s' 相关的信息。", request.query)
            );
        }

        return ToolResult.success(output.toString());
    }

    /**
     * 搜索请求模型
     */
    private static class SearchRequest {
        String query;
        String knowledgeBase;
        int topK;

        SearchRequest(String query, String knowledgeBase, int topK) {
            this.query = query;
            this.knowledgeBase = knowledgeBase;
            this.topK = Math.max(1, Math.min(topK, 10)); // 限制在1-10之间
        }
    }
}
