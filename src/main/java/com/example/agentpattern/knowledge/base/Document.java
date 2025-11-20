package com.example.agentpattern.knowledge.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档模型
 * 知识库中的基本单元
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    /**
     * 文档ID
     */
    private String id;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 文档来源
     */
    private String source;

    /**
     * 向量表示（可选）
     */
    private double[] embedding;

    /**
     * 创建时间
     */
    @Builder.Default
    private long createdAt = System.currentTimeMillis();

    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * 获取元数据
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * 文档摘要（前100个字符）
     */
    public String getSummary() {
        if (content == null) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
