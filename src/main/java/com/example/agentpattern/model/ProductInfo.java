package com.example.agentpattern.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 产品信息模型
 * 统一的产品数据结构，支持多品牌和多种产品类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {

    /**
     * 产品唯一标识
     */
    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 产品类别
     */
    private String category;

    /**
     * 价格
     */
    private String price;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品特性列表
     */
    private List<String> features;

    /**
     * 产品规格参数
     */
    private Map<String, String> specs;

    /**
     * 产品标签
     */
    private List<String> tags;

    /**
     * 判断产品是否匹配搜索关键词
     *
     * @param query 搜索关键词
     * @return 是否匹配
     */
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        // 匹配名称
        if (name != null && name.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // 匹配品牌
        if (brand != null && brand.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // 匹配类别
        if (category != null && category.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // 匹配描述
        if (description != null && description.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // 匹配标签
        if (tags != null) {
            for (String tag : tags) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }

        // 匹配特性
        if (features != null) {
            for (String feature : features) {
                if (feature.toLowerCase().contains(lowerQuery)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 格式化为文本输出
     *
     * @param includeSpecs 是否包含规格参数
     * @return 格式化的文本
     */
    public String toFormattedString(boolean includeSpecs) {
        StringBuilder sb = new StringBuilder();

        sb.append(name);
        if (brand != null) {
            sb.append(" (").append(brand).append(")");
        }
        sb.append("\n");

        sb.append("类别: ").append(category).append("\n");
        sb.append("价格: ").append(price).append("\n");

        if (description != null && !description.isEmpty()) {
            sb.append("描述: ").append(description).append("\n");
        }

        if (features != null && !features.isEmpty()) {
            sb.append("特性:\n");
            for (String feature : features) {
                sb.append("  - ").append(feature).append("\n");
            }
        }

        if (includeSpecs && specs != null && !specs.isEmpty()) {
            sb.append("规格参数:\n");
            specs.forEach((key, value) ->
                sb.append("  ").append(key).append(": ").append(value).append("\n")
            );
        }

        return sb.toString();
    }
}
