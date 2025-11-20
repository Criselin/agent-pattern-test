package com.example.agentpattern.knowledge.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 查询文本
     */
    private String query;

    /**
     * 匹配的文档列表
     */
    private List<ScoredDocument> documents;

    /**
     * 搜索耗时（毫秒）
     */
    private long searchTimeMs;

    /**
     * 使用的知识库名称
     */
    private String knowledgeBaseName;

    /**
     * 带评分的文档
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoredDocument {
        /**
         * 文档
         */
        private Document document;

        /**
         * 相似度分数（0-1之间）
         */
        private double score;

        /**
         * 排名
         */
        private int rank;
    }

    /**
     * 格式化为文本（供LLM使用）
     */
    public String formatForLLM() {
        if (documents == null || documents.isEmpty()) {
            return "未找到相关信息。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(documents.size()).append(" 条相关信息:\n\n");

        for (ScoredDocument scoredDoc : documents) {
            Document doc = scoredDoc.getDocument();
            sb.append("【").append(doc.getTitle()).append("】\n");
            sb.append(doc.getContent()).append("\n");
            if (doc.getSource() != null) {
                sb.append("来源: ").append(doc.getSource()).append("\n");
            }
            sb.append("相关度: ").append(String.format("%.2f", scoredDoc.getScore() * 100)).append("%\n");
            sb.append("\n");
        }

        return sb.toString();
    }
}
