package com.example.agentpattern.tools;

import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import com.example.agentpattern.loader.ProductDataLoader;
import com.example.agentpattern.model.ProductInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 产品搜索工具
 * 支持多品牌产品搜索，从配置文件动态加载产品数据
 */
@Slf4j
@Component
public class ProductSearchTool implements Tool {

    private final ToolRegistry toolRegistry;
    private final ProductDataLoader productDataLoader;

    public ProductSearchTool(ToolRegistry toolRegistry, ProductDataLoader productDataLoader) {
        this.toolRegistry = toolRegistry;
        this.productDataLoader = productDataLoader;
    }

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
        log.info("ProductSearchTool registered with {} products across {} brands",
                productDataLoader.getAllProducts().size(),
                productDataLoader.getAllBrands().size());
    }

    @Override
    public String getName() {
        return "product-search";
    }

    @Override
    public String getDescription() {
        return "Search for products by name, brand, or category. Supports multiple brands including Apple, Reolink, etc. " +
               "Input should be a product name, brand, or category keyword (e.g., 'iPhone', 'Reolink', '摄像头', 'MacBook')";
    }

    @Override
    public ToolResult execute(String input) {
        try {
            String query = input.trim();
            log.debug("Searching products with query: {}", query);

            // 使用产品加载器搜索
            List<ProductInfo> results = productDataLoader.searchProducts(query);

            if (results.isEmpty()) {
                // 提供搜索建议
                String suggestion = buildSearchSuggestion();
                return ToolResult.success("未找到匹配的产品。\n\n" + suggestion);
            }

            // 限制返回结果数量，避免输出过长
            int maxResults = 10;
            boolean hasMore = results.size() > maxResults;
            List<ProductInfo> limitedResults = results.stream()
                    .limit(maxResults)
                    .toList();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("找到 %d 个产品", results.size()));
            if (hasMore) {
                sb.append(String.format("，显示前 %d 个", maxResults));
            }
            sb.append(":\n\n");

            for (int i = 0; i < limitedResults.size(); i++) {
                ProductInfo product = limitedResults.get(i);
                sb.append(String.format("%d. ", i + 1));
                sb.append(product.toFormattedString(false));
                if (i < limitedResults.size() - 1) {
                    sb.append("\n");
                }
            }

            if (hasMore) {
                sb.append("\n提示: 还有 ")
                  .append(results.size() - maxResults)
                  .append(" 个产品未显示，请使用更具体的关键词缩小搜索范围。");
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
                      "description": "产品名称、品牌或类别关键词"
                    }
                  },
                  "required": ["query"]
                }
                """;
    }

    /**
     * 构建搜索建议
     */
    private String buildSearchSuggestion() {
        List<String> brands = productDataLoader.getAllBrands();
        List<String> categories = productDataLoader.getAllCategories();

        StringBuilder sb = new StringBuilder();
        sb.append("搜索建议:\n");

        if (!brands.isEmpty()) {
            sb.append("- 支持的品牌: ").append(String.join(", ", brands)).append("\n");
        }

        if (!categories.isEmpty()) {
            sb.append("- 支持的类别: ");
            // 只显示前10个类别
            List<String> limitedCategories = categories.stream().limit(10).toList();
            sb.append(String.join(", ", limitedCategories));
            if (categories.size() > 10) {
                sb.append(" 等");
            }
        }

        return sb.toString();
    }
}
