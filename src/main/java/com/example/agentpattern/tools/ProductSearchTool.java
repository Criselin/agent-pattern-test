package com.example.agentpattern.tools;

import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品搜索工具
 * 用于搜索产品信息
 */
@Slf4j
@Component
public class ProductSearchTool implements Tool {

    private final ToolRegistry toolRegistry;

    // 模拟产品数据库
    private static final List<ProductInfo> PRODUCT_DATABASE = new ArrayList<>();

    static {
        PRODUCT_DATABASE.add(new ProductInfo("iPhone 15 Pro", "手机", "¥7,999", "128GB/256GB/512GB, 钛金属设计"));
        PRODUCT_DATABASE.add(new ProductInfo("iPhone 15", "手机", "¥5,999", "128GB/256GB/512GB, 多彩铝金属设计"));
        PRODUCT_DATABASE.add(new ProductInfo("MacBook Pro 16", "笔记本电脑", "¥19,999", "M3 Pro芯片, 16GB内存, 512GB存储"));
        PRODUCT_DATABASE.add(new ProductInfo("MacBook Air 13", "笔记本电脑", "¥9,499", "M2芯片, 8GB内存, 256GB存储"));
        PRODUCT_DATABASE.add(new ProductInfo("iPad Pro 12.9", "平板电脑", "¥9,299", "M2芯片, 128GB存储, 支持Apple Pencil"));
        PRODUCT_DATABASE.add(new ProductInfo("iPad Air", "平板电脑", "¥4,799", "M1芯片, 64GB存储"));
        PRODUCT_DATABASE.add(new ProductInfo("AirPods Pro", "耳机", "¥1,899", "主动降噪, 自适应音频"));
        PRODUCT_DATABASE.add(new ProductInfo("AirPods Max", "耳机", "¥4,399", "头戴式, Hi-Fi音质"));
        PRODUCT_DATABASE.add(new ProductInfo("Apple Watch Series 9", "智能手表", "¥3,199", "健康监测, GPS"));
    }

    public ProductSearchTool(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
        log.info("ProductSearchTool registered");
    }

    @Override
    public String getName() {
        return "product-search";
    }

    @Override
    public String getDescription() {
        return "Search for products by name or category. Input should be a product name or category (e.g., 'iPhone', '耳机', 'MacBook')";
    }

    @Override
    public ToolResult execute(String input) {
        try {
            String query = input.trim().toLowerCase();
            log.debug("Searching products with query: {}", query);

            List<ProductInfo> results = PRODUCT_DATABASE.stream()
                    .filter(p -> p.name.toLowerCase().contains(query) ||
                               p.category.toLowerCase().contains(query))
                    .toList();

            if (results.isEmpty()) {
                return ToolResult.success("未找到匹配的产品。请尝试其他关键词。");
            }

            StringBuilder sb = new StringBuilder("找到以下产品:\n\n");
            for (int i = 0; i < results.size(); i++) {
                ProductInfo product = results.get(i);
                sb.append(String.format("%d. %s\n", i + 1, product.name));
                sb.append(String.format("   类别: %s\n", product.category));
                sb.append(String.format("   价格: %s\n", product.price));
                sb.append(String.format("   描述: %s\n", product.description));
                if (i < results.size() - 1) {
                    sb.append("\n");
                }
            }

            return ToolResult.success(sb.toString());

        } catch (Exception e) {
            log.error("Error searching products", e);
            return ToolResult.failure("搜索产品时出错: " + e.getMessage());
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
                      "description": "产品名称或类别关键词"
                    }
                  },
                  "required": ["query"]
                }
                """;
    }

    private static class ProductInfo {
        String name;
        String category;
        String price;
        String description;

        ProductInfo(String name, String category, String price, String description) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.description = description;
        }
    }
}
