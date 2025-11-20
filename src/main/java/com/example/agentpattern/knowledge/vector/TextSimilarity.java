package com.example.agentpattern.knowledge.vector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文本相似度计算工具
 * 提供多种文本相似度计算方法
 */
public class TextSimilarity {

    /**
     * 计算余弦相似度
     */
    public static double cosineSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 将文本转换为TF-IDF向量
     */
    public static double[] textToTfidfVector(String text, List<String> corpus) {
        // 分词
        List<String> tokens = tokenize(text);
        Set<String> uniqueTokens = new HashSet<>(tokens);

        // 构建词汇表
        Set<String> vocabulary = new HashSet<>();
        for (String doc : corpus) {
            vocabulary.addAll(tokenize(doc));
        }

        List<String> vocabList = new ArrayList<>(vocabulary);
        double[] vector = new double[vocabList.size()];

        // 计算TF-IDF
        for (int i = 0; i < vocabList.size(); i++) {
            String term = vocabList.get(i);
            double tf = termFrequency(term, tokens);
            double idf = inverseDocumentFrequency(term, corpus);
            vector[i] = tf * idf;
        }

        return vector;
    }

    /**
     * 简单的分词（基于空格和标点）
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为小写并分词
        String normalized = text.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5\\s]", " ");

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> !token.isEmpty() && token.length() > 1)
                .collect(Collectors.toList());
    }

    /**
     * 计算词频（TF）
     */
    private static double termFrequency(String term, List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0.0;
        }
        long count = tokens.stream().filter(t -> t.equals(term)).count();
        return (double) count / tokens.size();
    }

    /**
     * 计算逆文档频率（IDF）
     */
    private static double inverseDocumentFrequency(String term, List<String> corpus) {
        if (corpus.isEmpty()) {
            return 0.0;
        }

        long docsContainingTerm = corpus.stream()
                .filter(doc -> tokenize(doc).contains(term))
                .count();

        if (docsContainingTerm == 0) {
            return 0.0;
        }

        return Math.log((double) corpus.size() / docsContainingTerm);
    }

    /**
     * 计算BM25相似度（一个更高级的相似度算法）
     */
    public static double bm25Similarity(String query, String document, List<String> corpus) {
        List<String> queryTokens = tokenize(query);
        List<String> docTokens = tokenize(document);

        if (queryTokens.isEmpty() || docTokens.isEmpty()) {
            return 0.0;
        }

        double k1 = 1.5;
        double b = 0.75;

        double avgDocLength = corpus.stream()
                .mapToInt(doc -> tokenize(doc).size())
                .average()
                .orElse(0.0);

        double docLength = docTokens.size();
        double score = 0.0;

        for (String term : queryTokens) {
            double idf = inverseDocumentFrequency(term, corpus);
            long termFreq = docTokens.stream().filter(t -> t.equals(term)).count();

            double numerator = termFreq * (k1 + 1);
            double denominator = termFreq + k1 * (1 - b + b * (docLength / avgDocLength));

            score += idf * (numerator / denominator);
        }

        return score;
    }

    /**
     * 计算Jaccard相似度
     */
    public static double jaccardSimilarity(String text1, String text2) {
        Set<String> tokens1 = new HashSet<>(tokenize(text1));
        Set<String> tokens2 = new HashSet<>(tokenize(text2));

        if (tokens1.isEmpty() && tokens2.isEmpty()) {
            return 1.0;
        }

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }
}
