# Reolink 产品集成说明

本文档说明如何在 Agent Pattern Test 系统中集成 Reolink 产品咨询和推荐功能。

---

## 功能概述

系统现已支持 **Reolink 摄像头和安防产品** 的智能咨询和推荐服务，包括：

### 支持的功能

1. **产品搜索和推荐**
   - 按品牌搜索（Reolink）
   - 按类别搜索（摄像头、门铃、套装等）
   - 按特性搜索（4K、PoE、WiFi、全彩夜视等）
   - 价格查询

2. **产品知识库**
   - 产品使用手册和功能说明
   - 技术问题解答（WiFi连接、夜视、安装等）
   - 安装指南和最佳实践
   - 监控方案设计建议

3. **智能推荐**
   - 根据用户需求推荐合适产品
   - 场景化方案设计（家庭、商铺、停车场等）
   - 产品对比和选型建议

---

## 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────┐
│                  Agent ChatBot                       │
│                (智能客服机器人)                       │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        ↓                             ↓
┌───────────────────┐       ┌───────────────────────┐
│ ProductSearchTool │       │ KnowledgeSearchTool   │
│  (产品搜索工具)    │       │   (知识库检索工具)     │
└─────────┬─────────┘       └──────────┬────────────┘
          │                            │
          ↓                            ↓
┌──────────────────────┐     ┌────────────────────────┐
│ ProductDataLoader    │     │ KnowledgeBaseRegistry  │
│  (产品数据加载器)     │     │   (知识库注册表)        │
└─────────┬────────────┘     └──────────┬─────────────┘
          │                             │
          ↓                             ↓
┌──────────────────────┐     ┌────────────────────────┐
│ JSON 配置文件         │     │ ReolinkKnowledgeLoader │
│ - apple-products.json│     │   (Reolink知识加载器)   │
│ - reolink-products.json    └────────────────────────┘
└──────────────────────┘
```

### 数据流程

1. **用户提问** → Agent 分析意图
2. **产品搜索** → ProductSearchTool 调用 ProductDataLoader
3. **数据加载** → 从 JSON 文件读取产品信息
4. **知识检索** → KnowledgeSearchTool 检索相关文档
5. **智能推荐** → Agent 综合信息生成回复

---

## Reolink 产品数据

### 产品列表

系统内置 **8款** Reolink 产品：

| 产品名称 | 类别 | 价格 | 核心特性 |
|---------|------|------|---------|
| **Argus 4 Pro** | 无线摄像头 | ¥899 | 4K双镜头、180°广角、ColorX夜视、太阳能 |
| **RLC-810A** | 有线摄像头 | ¥699 | 4K PoE、智能AI检测、红外夜视30米 |
| **E1 Zoom** | 室内云台 | ¥329 | 5MP、3倍光学变焦、355°旋转、智能追踪 |
| **Duo 2 WiFi** | 双目摄像头 | ¥759 | 6MP双镜头、180°全景、ColorX夜视 |
| **TrackMix WiFi** | 双镜头云台 | ¥1,099 | 广角4K+长焦4MP、智能追踪、双画面 |
| **RLK8-800B4** | 监控套装 | ¥3,299 | 8路NVR+4个4K摄像头+2TB硬盘 |
| **Video Doorbell** | 智能门铃 | ¥599 | 5MP、180°广角、人形检测、双向对讲 |
| **Lumus** | 泛光灯摄像头 | ¥699 | 内置泛光灯+警报器、ColorX夜视 |

### 产品特性标签

- **分辨率**: 4K、6MP、5MP、1080P
- **连接方式**: WiFi、PoE、有线
- **夜视技术**: ColorX全彩、红外、泛光灯
- **供电方式**: 太阳能、电池、PoE、DC供电
- **智能功能**: AI检测、智能追踪、人车分类

---

## 知识库内容

### Reolink 产品手册库

**知识库ID**: `reolink-product-manual`

包含内容：
- Argus 4 Pro 产品特性详解
- PoE 摄像头优势与应用
- 智能门铃功能详解
- TrackMix 双镜头追踪技术

### Reolink 技术支持库

**知识库ID**: `reolink-tech-support`

包含内容：
- WiFi 连接问题排查
- 夜视效果优化
- NVR 录像机常见问题
- 设备故障诊断

### Reolink 安装指南库

**知识库ID**: `reolink-installation-guide`

包含内容：
- 户外摄像头安装最佳实践
- 监控系统方案设计
- 不同场景部署建议（家庭、商铺、停车场）
- 网络布线和供电方案

---

## 配置说明

### 启用 Reolink 功能

在 `application.yml` 中配置：

```yaml
# 知识库配置
knowledge:
  enabled: true
  load-sample-data: true      # Apple 产品知识库
  load-reolink-data: true     # Reolink 产品知识库（新增）

# 产品数据配置
products:
  enabled: true
  data-path: classpath:data/products/*.json
  supported-brands:
    - Apple
    - Reolink  # 新增 Reolink 品牌
```

### 禁用 Reolink 功能

如果不需要 Reolink 功能，可以：

**方式一：通过配置禁用**
```yaml
knowledge:
  load-reolink-data: false
```

**方式二：删除配置文件**
```bash
rm src/main/resources/data/products/reolink-products.json
```

**方式三：删除知识加载器**
```bash
rm src/main/java/com/example/agentpattern/knowledge/loader/ReolinkKnowledgeLoader.java
```

---

## 使用示例

### 示例 1: 产品搜索

**用户**: "有哪些 Reolink 摄像头？"

**系统响应**:
```
找到 8 个产品:

1. Reolink Argus 4 Pro (Reolink)
   类别: 无线摄像头
   价格: ¥899
   描述: 4K超高清双镜头，180°超广角，ColorX夜视...

2. Reolink RLC-810A (Reolink)
   类别: 有线摄像头
   价格: ¥699
   描述: 4K超高清PoE摄像头，智能人车检测...

...
```

### 示例 2: 按需推荐

**用户**: "我需要一款户外监控摄像头，要求夜视效果好，无需布线"

**Agent 工作流程**:
1. **分析需求**: 户外、夜视、无需布线 → WiFi摄像头 + 全彩夜视
2. **搜索产品**: 调用 `product-search` 工具，关键词 "Reolink 户外 WiFi"
3. **推荐**: Argus 4 Pro（太阳能供电，ColorX夜视）

### 示例 3: 技术问题咨询

**用户**: "Reolink 摄像头连接不上 WiFi 怎么办？"

**Agent 工作流程**:
1. **识别问题**: WiFi 连接问题
2. **检索知识库**: 调用 `knowledge-search`，搜索 "reolink-tech-support"
3. **返回方案**: 6步排查方法（检查频段、信号、设置、重置等）

### 示例 4: 方案设计

**用户**: "我有一个小商铺，想装监控，预算5000元，有什么方案？"

**Agent 工作流程**:
1. **理解需求**: 商铺、预算5000
2. **检索方案**: 从安装指南知识库检索"商铺监控方案"
3. **推荐配置**:
   - RLK8-800B4 套装（¥3,299）
   - TrackMix WiFi（¥1,099，收银区）
   - Duo 2 WiFi（¥759，入口）
   - 总计：¥5,157

---

## 测试方法

### 自动化测试

运行提供的测试脚本：

```bash
# 确保应用正在运行
mvn spring-boot:run

# 在另一个终端运行测试
./test-reolink.sh
```

测试脚本包含 **10个测试场景**:
1. Reolink 品牌搜索
2. 摄像头类别搜索
3. 具体产品咨询
4. 4K 摄像头搜索
5. 价格查询
6. 监控套装咨询
7. 技术问题咨询
8. WiFi 连接问题
9. PoE 摄像头咨询
10. 混合品牌搜索

### 手动测试

```bash
# 测试 1: 品牌搜索
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "有哪些 Reolink 产品？"}'

# 测试 2: 产品咨询
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Argus 4 Pro 有什么特点？"}'

# 测试 3: 技术支持
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Reolink 摄像头怎么安装？"}'
```

---

## 扩展新品牌

系统采用**可扩展架构**，添加新品牌只需3步：

### 步骤 1: 创建产品数据文件

在 `src/main/resources/data/products/` 目录下创建新文件：

```bash
# 例如添加 Hikvision 品牌
touch src/main/resources/data/products/hikvision-products.json
```

文件格式参考 `reolink-products.json`：

```json
[
  {
    "id": "product-id",
    "name": "产品名称",
    "brand": "品牌名",
    "category": "类别",
    "price": "¥999",
    "description": "产品描述",
    "features": ["特性1", "特性2"],
    "specs": {
      "规格名": "规格值"
    },
    "tags": ["标签1", "标签2"]
  }
]
```

### 步骤 2: 创建知识库加载器（可选）

```java
@Component
public class HikvisionKnowledgeLoader {

    private final KnowledgeBaseRegistry registry;

    @PostConstruct
    public void loadKnowledge() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
            "hikvision-manual",
            "Hikvision 产品手册"
        );

        // 添加文档...
        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);
    }
}
```

### 步骤 3: 更新配置

```yaml
products:
  supported-brands:
    - Apple
    - Reolink
    - Hikvision  # 新增品牌
```

**无需修改任何业务代码！** ProductDataLoader 会自动加载新的配置文件。

---

## 性能优化

### 产品数据加载

- **加载时机**: 应用启动时一次性加载
- **存储方式**: 内存 ConcurrentHashMap
- **搜索性能**: O(n)，n为产品总数
- **建议规模**: 单品牌 < 200 个产品

### 知识库检索

- **向量化**: TF-IDF + 余弦相似度
- **检索性能**: O(n)，n为文档总数
- **建议规模**: 单知识库 < 1000 个文档
- **优化方向**: 集成向量数据库（Pinecone、Weaviate等）

### 缓存策略

当前未实现缓存，未来可考虑：
- Redis 缓存热门查询结果
- LLM 响应缓存（相同问题）
- 产品数据定期更新机制

---

## API 变更

### 原有 API（向后兼容）

```bash
# 产品搜索（现支持多品牌）
POST /api/chat
{
  "message": "搜索产品关键词"
}
```

### 工具描述更新

```
product-search:
  旧描述: "Search for products by name or category..."
  新描述: "Search for products by name, brand, or category.
           Supports multiple brands including Apple, Reolink, etc."
```

### 新增知识库

- `reolink-product-manual`
- `reolink-tech-support`
- `reolink-installation-guide`

---

## 数据维护

### 产品数据更新

编辑 `src/main/resources/data/products/reolink-products.json`：

```json
{
  "id": "reolink-new-product",
  "name": "新产品名称",
  ...
}
```

重启应用即可生效（未来可支持热更新）。

### 知识库更新

编辑 `ReolinkKnowledgeLoader.java`，添加新文档：

```java
documents.add(Document.builder()
    .title("新知识标题")
    .content("知识内容...")
    .source("来源")
    .build());
```

---

## 常见问题

### Q1: 为什么搜索不到 Reolink 产品？

**检查步骤**:
1. 确认配置 `knowledge.load-reolink-data: true`
2. 检查文件是否存在 `src/main/resources/data/products/reolink-products.json`
3. 查看启动日志，确认加载成功
4. 重启应用

### Q2: 知识库检索没有返回 Reolink 相关内容？

**原因**:
- 查询关键词匹配度低
- 知识库未加载

**解决**:
- 使用更具体的关键词
- 检查 `ReolinkKnowledgeLoader` 是否被 Spring 扫描
- 查看日志确认知识库已加载

### Q3: 如何只保留 Reolink，移除 Apple？

**方法**:
1. 删除 `apple-products.json`
2. 配置 `knowledge.load-sample-data: false`
3. 重启应用

### Q4: 产品数据可以存储在数据库吗？

**可以**，实现步骤：
1. 创建 `DatabaseProductLoader` 实现
2. 从数据库读取产品数据
3. 转换为 `ProductInfo` 对象
4. 替换 `ProductDataLoader`

---

## 技术细节

### 类和接口

| 类名 | 作用 | 路径 |
|------|------|------|
| `ProductInfo` | 产品数据模型 | `model/ProductInfo.java` |
| `ProductDataLoader` | 产品数据加载器 | `loader/ProductDataLoader.java` |
| `ProductSearchTool` | 产品搜索工具 | `tools/ProductSearchTool.java` |
| `ReolinkKnowledgeLoader` | Reolink 知识加载器 | `knowledge/loader/ReolinkKnowledgeLoader.java` |

### 配置文件

| 文件 | 作用 |
|------|------|
| `application.yml` | 主配置文件 |
| `reolink-products.json` | Reolink 产品数据 |
| `apple-products.json` | Apple 产品数据 |

### 依赖关系

```
ProductSearchTool
    ↓ 依赖
ProductDataLoader
    ↓ 读取
JSON 配置文件 (*.json)
```

---

## 未来改进

### 短期（1-2周）
- [ ] 支持产品对比功能
- [ ] 添加更多 Reolink 产品
- [ ] 增强搜索过滤（价格区间、特性筛选）

### 中期（1-2月）
- [ ] 集成向量数据库（提升检索性能）
- [ ] 支持产品图片展示
- [ ] 添加用户评价和推荐理由

### 长期（3-6月）
- [ ] 多模态支持（图片识别产品）
- [ ] 个性化推荐算法
- [ ] 集成真实电商API

---

## 总结

Reolink 产品集成采用**无侵入式设计**：

✅ **无需修改核心代码** - 只需添加配置文件
✅ **完全向后兼容** - 现有功能不受影响
✅ **易于扩展** - 添加新品牌仅需3步
✅ **性能优化** - 启动加载，内存缓存
✅ **智能推荐** - LLM理解需求，精准匹配

这种设计使得系统可以**轻松支持任意品牌**的产品，实现真正的多品牌智能客服机器人。
