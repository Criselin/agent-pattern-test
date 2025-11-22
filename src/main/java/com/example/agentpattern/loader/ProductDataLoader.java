package com.example.agentpattern.loader;

import com.example.agentpattern.model.ProductInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 产品数据加载器
 * 从配置文件加载产品数据，支持多品牌和动态扩展
 */
@Slf4j
@Component
public class ProductDataLoader {

    private final ObjectMapper objectMapper;
    private final Map<String, List<ProductInfo>> productsByBrand = new ConcurrentHashMap<>();
    private final List<ProductInfo> allProducts = new ArrayList<>();

    // 产品数据文件路径模式
    private static final String PRODUCT_FILES_PATTERN = "classpath:data/products/*.json";

    public ProductDataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadProducts() {
        log.info("开始加载产品数据...");

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(PRODUCT_FILES_PATTERN);

            if (resources.length == 0) {
                log.warn("未找到产品数据文件: {}", PRODUCT_FILES_PATTERN);
                return;
            }

            for (Resource resource : resources) {
                loadProductFile(resource);
            }

            log.info("产品数据加载完成，共加载 {} 个品牌，{} 个产品",
                    productsByBrand.size(), allProducts.size());

            // 打印品牌统计
            productsByBrand.forEach((brand, products) ->
                log.info("  - {}: {} 个产品", brand, products.size())
            );

        } catch (IOException e) {
            log.error("加载产品数据失败", e);
        }
    }

    /**
     * 加载单个产品文件
     */
    private void loadProductFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            String filename = resource.getFilename();
            log.debug("加载产品文件: {}", filename);

            List<ProductInfo> products = objectMapper.readValue(
                is,
                new TypeReference<List<ProductInfo>>() {}
            );

            if (products == null || products.isEmpty()) {
                log.warn("文件 {} 中没有产品数据", filename);
                return;
            }

            for (ProductInfo product : products) {
                // 添加到总列表
                allProducts.add(product);

                // 按品牌分组
                String brand = product.getBrand();
                if (brand != null) {
                    productsByBrand
                        .computeIfAbsent(brand, k -> new ArrayList<>())
                        .add(product);
                }
            }

            log.info("从 {} 加载了 {} 个产品", filename, products.size());

        } catch (IOException e) {
            log.error("加载产品文件失败: {}", resource.getFilename(), e);
        }
    }

    /**
     * 获取所有产品
     */
    public List<ProductInfo> getAllProducts() {
        return new ArrayList<>(allProducts);
    }

    /**
     * 按品牌获取产品
     */
    public List<ProductInfo> getProductsByBrand(String brand) {
        return productsByBrand.getOrDefault(brand, new ArrayList<>());
    }

    /**
     * 搜索产品
     *
     * @param query 搜索关键词
     * @return 匹配的产品列表
     */
    public List<ProductInfo> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        return allProducts.stream()
                .filter(product -> product.matches(query))
                .toList();
    }

    /**
     * 按品牌和类别搜索
     */
    public List<ProductInfo> searchProducts(String brand, String category) {
        return allProducts.stream()
                .filter(p -> (brand == null || brand.equalsIgnoreCase(p.getBrand())))
                .filter(p -> (category == null ||
                             (p.getCategory() != null &&
                              p.getCategory().toLowerCase().contains(category.toLowerCase()))))
                .toList();
    }

    /**
     * 根据ID获取产品
     */
    public ProductInfo getProductById(String id) {
        return allProducts.stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有品牌
     */
    public List<String> getAllBrands() {
        return new ArrayList<>(productsByBrand.keySet());
    }

    /**
     * 获取所有类别
     */
    public List<String> getAllCategories() {
        return allProducts.stream()
                .map(ProductInfo::getCategory)
                .distinct()
                .toList();
    }
}
