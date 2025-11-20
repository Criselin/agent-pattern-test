package com.example.agentpattern.knowledge.base;

import java.util.List;

/**
 * 知识库接口
 * 定义知识库的基本操作
 */
public interface KnowledgeBase {

    /**
     * 获取知识库名称
     */
    String getName();

    /**
     * 获取知识库描述
     */
    String getDescription();

    /**
     * 获取知识库类型
     */
    String getType();

    /**
     * 搜索相关文档
     *
     * @param query 查询文本
     * @param topK 返回前K个结果
     * @return 搜索结果
     */
    SearchResult search(String query, int topK);

    /**
     * 搜索相关文档（默认返回前3个）
     */
    default SearchResult search(String query) {
        return search(query, 3);
    }

    /**
     * 添加文档到知识库
     *
     * @param document 文档
     */
    void addDocument(Document document);

    /**
     * 批量添加文档
     *
     * @param documents 文档列表
     */
    void addDocuments(List<Document> documents);

    /**
     * 获取知识库中的文档数量
     */
    int getDocumentCount();

    /**
     * 根据ID获取文档
     */
    Document getDocument(String id);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void deleteDocument(String id);

    /**
     * 清空知识库
     */
    void clear();

    /**
     * 知识库是否已初始化
     */
    boolean isInitialized();
}
