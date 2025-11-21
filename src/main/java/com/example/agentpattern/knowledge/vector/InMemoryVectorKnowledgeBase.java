package com.example.agentpattern.knowledge.vector;

import com.example.agentpattern.knowledge.base.Document;
import com.example.agentpattern.knowledge.base.KnowledgeBase;
import com.example.agentpattern.knowledge.base.SearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于内存的向量知识库
 * 使用TF-IDF和余弦相似度进行文档检索
 */
@Slf4j
public class InMemoryVectorKnowledgeBase implements KnowledgeBase {

    private final String name;
    private final String description;
    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    // 缓存的语料库（用于TF-IDF计算）
    private volatile List<String> corpus = new ArrayList<>();

    public InMemoryVectorKnowledgeBase(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return "VECTOR_MEMORY";
    }

    @Override
    public SearchResult search(String query, int topK) {
        long startTime = System.currentTimeMillis();

        if (documents.isEmpty()) {
            log.warn("Knowledge base '{}' is empty", name);
            return SearchResult.builder()
                    .query(query)
                    .documents(Collections.emptyList())
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .knowledgeBaseName(name)
                    .build();
        }

        try {
            // 计算查询文本的向量
            double[] queryVector = TextSimilarity.textToTfidfVector(query, corpus);

            // 计算与所有文档的相似度
            List<SearchResult.ScoredDocument> scoredDocs = new ArrayList<>();

            for (Document doc : documents.values()) {
                // 如果文档没有向量，计算它
                if (doc.getEmbedding() == null) {
                    double[] docVector = TextSimilarity.textToTfidfVector(
                            doc.getTitle() + " " + doc.getContent(),
                            corpus
                    );
                    doc.setEmbedding(docVector);
                }

                // 计算相似度
                double similarity = TextSimilarity.cosineSimilarity(queryVector, doc.getEmbedding());

                // 也可以使用BM25作为补充
                double bm25Score = TextSimilarity.bm25Similarity(
                        query,
                        doc.getTitle() + " " + doc.getContent(),
                        corpus
                );

                // 综合两种分数（可以调整权重）
                double finalScore = 0.6 * similarity + 0.4 * Math.min(bm25Score / 10.0, 1.0);

                scoredDocs.add(SearchResult.ScoredDocument.builder()
                        .document(doc)
                        .score(finalScore)
                        .build());
            }

            // 按分数排序并取TopK
            List<SearchResult.ScoredDocument> topResults = scoredDocs.stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(topK)
                    .collect(Collectors.toList());

            // 设置排名
            for (int i = 0; i < topResults.size(); i++) {
                topResults.get(i).setRank(i + 1);
            }

            long searchTime = System.currentTimeMillis() - startTime;
            log.debug("Search completed in {}ms, found {} results", searchTime, topResults.size());

            return SearchResult.builder()
                    .query(query)
                    .documents(topResults)
                    .searchTimeMs(searchTime)
                    .knowledgeBaseName(name)
                    .build();

        } catch (Exception e) {
            log.error("Error searching knowledge base '{}'", name, e);
            return SearchResult.builder()
                    .query(query)
                    .documents(Collections.emptyList())
                    .searchTimeMs(System.currentTimeMillis() - startTime)
                    .knowledgeBaseName(name)
                    .build();
        }
    }

    @Override
    public void addDocument(Document document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("Document and document ID cannot be null");
        }

        documents.put(document.getId(), document);
        rebuildCorpus();
        initialized = true;

        log.debug("Added document '{}' to knowledge base '{}'", document.getId(), name);
    }

    @Override
    public void addDocuments(List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }

        for (Document doc : docs) {
            if (doc != null && doc.getId() != null) {
                documents.put(doc.getId(), doc);
            }
        }

        rebuildCorpus();
        initialized = true;

        log.info("Added {} documents to knowledge base '{}'", docs.size(), name);
    }

    @Override
    public int getDocumentCount() {
        return documents.size();
    }

    @Override
    public Document getDocument(String id) {
        return documents.get(id);
    }

    @Override
    public void deleteDocument(String id) {
        Document removed = documents.remove(id);
        if (removed != null) {
            rebuildCorpus();
            log.debug("Deleted document '{}' from knowledge base '{}'", id, name);
        }
    }

    @Override
    public void clear() {
        documents.clear();
        corpus.clear();
        initialized = false;
        log.info("Cleared knowledge base '{}'", name);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 重建语料库（用于TF-IDF计算）
     */
    private void rebuildCorpus() {
        corpus = documents.values().stream()
                .map(doc -> doc.getTitle() + " " + doc.getContent())
                .collect(Collectors.toList());

        // 清除所有文档的向量缓存，下次搜索时重新计算
        documents.values().forEach(doc -> doc.setEmbedding(null));
    }
}
