# 本地测试运行和数据准备指南

本文档提供详细的本地环境搭建、测试运行和数据准备方案，帮助你快速在本地部署和测试 Agent Pattern Test 系统。

---

## 目录

1. [环境准备](#1-环境准备)
2. [项目配置](#2-项目配置)
3. [数据准备方案](#3-数据准备方案)
4. [启动应用](#4-启动应用)
5. [本地测试方案](#5-本地测试方案)
6. [测试场景](#6-测试场景)
7. [性能测试](#7-性能测试)
8. [常见问题排查](#8-常见问题排查)

---

## 1. 环境准备

### 1.1 必需软件

#### Java 17+
```bash
# 检查 Java 版本
java -version

# 如果未安装，下载 OpenJDK 17
# macOS (使用 Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Windows
# 从 https://adoptium.net/ 下载安装
```

#### Maven 3.6+
```bash
# 检查 Maven 版本
mvn -version

# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Windows
# 从 https://maven.apache.org/download.cgi 下载安装
```

### 1.2 获取 OpenAI API Key

#### 方式一：使用 OpenAI（推荐）

1. 访问 [OpenAI Platform](https://platform.openai.com/signup)
2. 注册并登录账号
3. 进入 [API Keys 页面](https://platform.openai.com/api-keys)
4. 点击 "Create new secret key"
5. 复制并保存 API Key（格式：`sk-...`）

**费用说明**：
- GPT-4: ~$0.03/1K tokens (输入), ~$0.06/1K tokens (输出)
- GPT-3.5-turbo: ~$0.001/1K tokens (更便宜，但效果较差)
- 建议初次测试充值 $10-20

#### 方式二：使用 Azure OpenAI

1. 登录 [Azure Portal](https://portal.azure.com)
2. 创建 "Azure OpenAI" 资源
3. 获取以下信息：
   - API Key
   - Endpoint URL（如：`https://your-resource.openai.azure.com`）
   - Deployment Name（部署的模型名称，如：`gpt-4`）

---

## 2. 项目配置

### 2.1 克隆项目（如果还没有）

```bash
git clone <repository-url>
cd agent-pattern-test
```

### 2.2 配置 OpenAI（默认）

创建或编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: sk-your-actual-api-key-here  # 替换为你的真实 API Key
      model: gpt-4  # 或 gpt-3.5-turbo（更便宜）
      base-url: https://api.openai.com
```

**或使用环境变量（推荐，更安全）：**

```bash
# macOS/Linux
export OPENAI_API_KEY="sk-your-actual-api-key-here"

# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-actual-api-key-here"

# Windows (CMD)
set OPENAI_API_KEY=sk-your-actual-api-key-here
```

### 2.3 配置 Azure OpenAI（可选）

编辑 `src/main/resources/application-azure-openai.yml`：

```yaml
spring:
  ai:
    azure:
      openai:
        api-key: your-azure-api-key
        endpoint: https://your-resource.openai.azure.com
        deployment-name: gpt-4
```

**或使用环境变量：**

```bash
export AZURE_OPENAI_API_KEY="your-azure-api-key"
export AZURE_OPENAI_ENDPOINT="https://your-resource.openai.azure.com"
export AZURE_OPENAI_DEPLOYMENT_NAME="gpt-4"
```

### 2.4 调整配置参数（可选）

编辑 `src/main/resources/application.yml`：

```yaml
# Agent 配置
agent:
  orchestrator:
    default: react  # 编排器：react 或 plan-execute

  react:
    max-iterations: 5  # ReAct 最大迭代次数（减少可节省成本）
    enable-logging: true

  plan-execute:
    max-steps: 10
    replan-threshold: 3

# 知识库配置
knowledge:
  enabled: true
  load-sample-data: true  # 是否加载示例数据

# 会话管理
session:
  default-ttl-minutes: 60
  auto-cleanup: true

# 日志级别
logging:
  level:
    com.example.agentpattern: DEBUG  # 查看详细日志
```

---

## 3. 数据准备方案

### 3.1 内置示例数据

系统启动时会自动加载以下示例数据，无需额外配置：

#### 订单数据（OrderQueryTool）

| 订单号 | 商品 | 状态 | 下单时间 | 物流信息 |
|--------|------|------|----------|----------|
| ORD001 | iPhone 15 Pro | 已发货 | 2024-01-15 | 顺丰快递: SF1234567890 |
| ORD002 | MacBook Pro 16 | 配送中 | 2024-01-16 | 京东物流: JD2345678901 |
| ORD003 | AirPods Pro | 已签收 | 2024-01-10 | 中通快递: ZTO3456789012 |
| ORD004 | iPad Air | 处理中 | 2024-01-18 | 待发货 |

#### 产品数据（ProductSearchTool）

- **iPhone 系列**：iPhone 15 Pro、iPhone 15、iPhone 14
- **MacBook 系列**：MacBook Air 13、MacBook Pro 14/16
- **iPad 系列**：iPad Pro、iPad Air、iPad mini
- **配件系列**：AirPods Pro、AirPods、Apple Watch

#### FAQ 数据（FAQTool）

- 退货政策
- 配送说明
- 支付方式
- 保修服务
- 发票开具
- 换货流程

#### 知识库数据（KnowledgeSearchTool）

**产品手册库（product-manual）**：
- iPhone 使用指南
- MacBook 技术规格
- iPad 功能介绍
- AirPods 配对说明
- Apple Watch 健康功能

**技术支持库（tech-support）**：
- Mac 运行缓慢优化
- iPhone 无法开机
- AirPods 连接问题
- iPad 屏幕触控失灵
- 电池续航优化

**公司政策库（company-policy）**：
- 退换货政策详解
- 保修条款说明
- 服务承诺
- 隐私政策

### 3.2 自定义数据准备

#### 方式一：修改工具类数据（简单）

**1. 添加订单数据**

编辑 `src/main/java/com/example/agentpattern/tools/OrderQueryTool.java`：

```java
private Map<String, Map<String, String>> initOrders() {
    Map<String, Map<String, String>> orders = new HashMap<>();

    // 添加你的测试订单
    orders.put("ORD005", Map.of(
        "orderId", "ORD005",
        "product", "您的商品名称",
        "status", "配送中",
        "orderDate", "2024-01-20",
        "logistics", "快递公司: 单号"
    ));

    return orders;
}
```

**2. 添加产品数据**

编辑 `src/main/java/com/example/agentpattern/tools/ProductSearchTool.java`：

```java
private List<Map<String, String>> initProducts() {
    List<Map<String, String>> products = new ArrayList<>();

    // 添加你的产品
    products.add(Map.of(
        "name", "产品名称",
        "category", "类别",
        "price", "¥9,999",
        "description", "产品描述",
        "stock", "有货"
    ));

    return products;
}
```

**3. 添加 FAQ 数据**

编辑 `src/main/java/com/example/agentpattern/tools/FAQTool.java`：

```java
private Map<String, String> initFAQs() {
    Map<String, String> faqs = new HashMap<>();

    // 添加你的问题
    faqs.put("你的问题关键词", "详细回答内容...");

    return faqs;
}
```

#### 方式二：扩展知识库数据（推荐）

创建自定义知识加载器：

```java
package com.example.agentpattern.knowledge.loader;

import com.example.agentpattern.knowledge.base.*;
import com.example.agentpattern.knowledge.vector.InMemoryVectorKnowledgeBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "knowledge", name = "load-custom-data", havingValue = "true")
public class CustomKnowledgeLoader {

    private final KnowledgeBaseRegistry registry;

    @PostConstruct
    public void loadCustomData() {
        log.info("加载自定义知识库数据...");

        // 创建自定义知识库
        KnowledgeBase customKB = new InMemoryVectorKnowledgeBase(
            "custom-kb",
            "自定义知识库"
        );

        // 准备文档数据
        List<Document> documents = Arrays.asList(
            Document.builder()
                .id(UUID.randomUUID().toString())
                .title("文档标题1")
                .content("文档详细内容...")
                .source("数据来源")
                .metadata(Map.of("category", "分类"))
                .build(),

            Document.builder()
                .id(UUID.randomUUID().toString())
                .title("文档标题2")
                .content("更多内容...")
                .source("数据来源")
                .build()
        );

        // 添加文档到知识库
        customKB.addDocuments(documents);

        // 注册知识库
        registry.registerKnowledgeBase(customKB);

        log.info("自定义知识库加载完成，共 {} 个文档", documents.size());
    }
}
```

然后在 `application.yml` 中启用：

```yaml
knowledge:
  load-custom-data: true
```

#### 方式三：从文件加载数据

创建 JSON 数据文件 `src/main/resources/data/custom-knowledge.json`：

```json
[
  {
    "title": "产品特性说明",
    "content": "详细的产品特性描述...",
    "source": "产品文档",
    "category": "产品"
  },
  {
    "title": "技术问题解决",
    "content": "常见技术问题的解决方案...",
    "source": "技术支持",
    "category": "技术"
  }
]
```

创建文件加载器：

```java
@Component
@RequiredArgsConstructor
public class FileKnowledgeLoader {

    private final KnowledgeBaseRegistry registry;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void loadFromFile() throws IOException {
        // 读取 JSON 文件
        InputStream is = getClass()
            .getResourceAsStream("/data/custom-knowledge.json");

        List<DocumentData> dataList = objectMapper.readValue(
            is,
            new TypeReference<List<DocumentData>>() {}
        );

        // 转换为 Document 对象并加载
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase("file-kb", "文件知识库");

        List<Document> docs = dataList.stream()
            .map(data -> Document.builder()
                .id(UUID.randomUUID().toString())
                .title(data.getTitle())
                .content(data.getContent())
                .source(data.getSource())
                .metadata(Map.of("category", data.getCategory()))
                .build())
            .toList();

        kb.addDocuments(docs);
        registry.registerKnowledgeBase(kb);
    }

    @Data
    private static class DocumentData {
        private String title;
        private String content;
        private String source;
        private String category;
    }
}
```

### 3.3 数据量建议

**本地测试建议数据规模**：

- **订单数据**：10-50 条（测试查询功能）
- **产品数据**：20-100 个（测试搜索功能）
- **FAQ 数据**：10-30 个（测试匹配功能）
- **知识库文档**：50-200 个（测试 RAG 检索）

**性能考虑**：
- 当前使用内存存储，单个知识库建议不超过 10000 个文档
- 文档内容建议控制在 500-2000 字
- 如需更大规模，建议集成向量数据库（Pinecone、Weaviate 等）

---

## 4. 启动应用

### 4.1 编译项目

```bash
# 进入项目目录
cd agent-pattern-test

# 清理并编译
mvn clean package

# 如果遇到测试失败，可以跳过测试
mvn clean package -DskipTests
```

### 4.2 运行应用

#### 方式一：使用 Maven（开发推荐）

```bash
# 使用 OpenAI（默认）
mvn spring-boot:run

# 使用 Azure OpenAI
mvn spring-boot:run -Dspring-boot.run.profiles=azure-openai

# 使用 GPT-3.5（更便宜）
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.ai.openai.model=gpt-3.5-turbo"
```

#### 方式二：运行 JAR 包（生产推荐）

```bash
# 编译
mvn clean package -DskipTests

# 运行（OpenAI）
java -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar

# 运行（Azure OpenAI）
java -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=azure-openai

# 运行（自定义端口）
java -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar \
  --server.port=9090
```

#### 方式三：IDE 运行

1. 打开 IDEA/Eclipse
2. 导入 Maven 项目
3. 找到 `AgentPatternApplication.java`
4. 右键 → Run 'AgentPatternApplication'

### 4.3 验证启动成功

启动成功后，你应该看到类似日志：

```
2024-01-20 10:00:00 - 正在加载示例知识库数据...
2024-01-20 10:00:01 - 知识库 'product-manual' 加载完成，共32个文档
2024-01-20 10:00:01 - 知识库 'tech-support' 加载完成，共28个文档
2024-01-20 10:00:01 - 知识库 'company-policy' 加载完成，共15个文档
2024-01-20 10:00:02 - Tomcat started on port(s): 8080 (http)
2024-01-20 10:00:02 - Started AgentPatternApplication in 3.456 seconds
```

访问健康检查：

```bash
curl http://localhost:8080/api/chat/health

# 预期响应
{
  "status": "UP",
  "timestamp": "2024-01-20T10:00:00"
}
```

---

## 5. 本地测试方案

### 5.1 使用 cURL 测试

#### 测试 1: 获取欢迎消息

```bash
curl http://localhost:8080/api/chat/welcome
```

**预期响应**：
```json
{
  "message": "您好！我是智能客服助手，很高兴为您服务。我可以帮您查询订单、搜索产品或解答常见问题。",
  "success": true
}
```

#### 测试 2: 订单查询

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我查询订单ORD001的物流信息"
  }'
```

**预期响应**：
```json
{
  "message": "您的订单ORD001（iPhone 15 Pro）已发货，物流信息如下：\n- 物流公司: 顺丰快递\n- 快递单号: SF1234567890\n- 下单时间: 2024-01-15",
  "session_id": "auto-generated-session-id",
  "success": true,
  "execution_time_ms": 2345,
  "steps": [
    {
      "thought": "用户想查询订单ORD001的物流信息，我需要使用订单查询工具",
      "action": "order-query",
      "action_input": "ORD001",
      "observation": "订单信息:\n订单号: ORD001\n商品: iPhone 15 Pro\n..."
    },
    {
      "thought": "我已经获取到了订单的物流信息，可以回答用户了",
      "action": null,
      "action_input": null,
      "observation": null
    }
  ]
}
```

#### 测试 3: 产品搜索

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我想买MacBook，有什么推荐的吗？"
  }'
```

#### 测试 4: 常见问题

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你们的退货政策是什么？"
  }'
```

#### 测试 5: 知识库检索（RAG）

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "iPhone电池如何保养？"
  }'
```

#### 测试 6: 多轮对话

```bash
# 第一轮：创建会话
RESPONSE=$(curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我想买iPhone"}')

# 提取 session_id
SESSION_ID=$(echo $RESPONSE | jq -r '.session_id')

# 第二轮：继续对话
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{
    \"message\": \"那它的价格是多少？\",
    \"session_id\": \"$SESSION_ID\"
  }"
```

### 5.2 使用 Postman 测试

#### 导入 Postman Collection

创建 `postman_collection.json`：

```json
{
  "info": {
    "name": "Agent Pattern Test API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "欢迎消息",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/chat/welcome",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "chat", "welcome"]
        }
      }
    },
    {
      "name": "订单查询",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"查询订单ORD001\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/chat",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "chat"]
        }
      }
    },
    {
      "name": "产品搜索",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"推荐一款MacBook\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/chat",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "chat"]
        }
      }
    },
    {
      "name": "知识库检索",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"message\": \"MacBook运行很慢怎么办？\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/chat",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "chat"]
        }
      }
    },
    {
      "name": "获取统计信息",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/chat/stats",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "chat", "stats"]
        }
      }
    },
    {
      "name": "会话列表",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/sessions",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "sessions"]
        }
      }
    },
    {
      "name": "会话详情",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/sessions/{{session_id}}",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "sessions", "{{session_id}}"]
        }
      }
    },
    {
      "name": "热门话题",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/api/sessions/analytics/hot-topics?topN=10",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "sessions", "analytics", "hot-topics"],
          "query": [
            {
              "key": "topN",
              "value": "10"
            }
          ]
        }
      }
    }
  ]
}
```

在 Postman 中：
1. File → Import
2. 选择上面的 JSON 文件
3. 即可看到所有测试请求

### 5.3 创建测试脚本

创建 `test.sh` 脚本：

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "======================================"
echo "Agent Pattern Test - 自动化测试"
echo "======================================"

# 测试 1: 健康检查
echo -e "\n[测试 1] 健康检查..."
response=$(curl -s $BASE_URL/api/chat/health)
if echo $response | grep -q "UP"; then
    echo -e "${GREEN}✓ 健康检查通过${NC}"
else
    echo -e "${RED}✗ 健康检查失败${NC}"
    exit 1
fi

# 测试 2: 欢迎消息
echo -e "\n[测试 2] 欢迎消息..."
response=$(curl -s $BASE_URL/api/chat/welcome)
if echo $response | grep -q "智能客服"; then
    echo -e "${GREEN}✓ 欢迎消息正常${NC}"
else
    echo -e "${RED}✗ 欢迎消息失败${NC}"
fi

# 测试 3: 订单查询
echo -e "\n[测试 3] 订单查询..."
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询订单ORD001"}')
if echo $response | grep -q "ORD001"; then
    echo -e "${GREEN}✓ 订单查询成功${NC}"
    echo "响应: $(echo $response | jq -r '.message' | head -c 100)..."
else
    echo -e "${RED}✗ 订单查询失败${NC}"
fi

# 测试 4: 产品搜索
echo -e "\n[测试 4] 产品搜索..."
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "推荐一款iPhone"}')
if echo $response | grep -q "iPhone"; then
    echo -e "${GREEN}✓ 产品搜索成功${NC}"
else
    echo -e "${RED}✗ 产品搜索失败${NC}"
fi

# 测试 5: FAQ
echo -e "\n[测试 5] 常见问题..."
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "退货政策"}')
if echo $response | grep -q "退货"; then
    echo -e "${GREEN}✓ FAQ 查询成功${NC}"
else
    echo -e "${RED}✗ FAQ 查询失败${NC}"
fi

# 测试 6: 会话管理
echo -e "\n[测试 6] 会话管理..."
response=$(curl -s $BASE_URL/api/sessions)
if echo $response | grep -q "sessionId" || echo $response | grep -q "\[\]"; then
    echo -e "${GREEN}✓ 会话管理正常${NC}"
else
    echo -e "${RED}✗ 会话管理失败${NC}"
fi

echo -e "\n======================================"
echo "测试完成！"
echo "======================================"
```

运行测试：

```bash
chmod +x test.sh
./test.sh
```

---

## 6. 测试场景

### 6.1 基础功能测试

#### 场景 1: 单工具调用
```bash
# 订单查询
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询订单ORD002的状态"}'

# 预期：调用 order-query 工具，返回订单信息
```

#### 场景 2: 多工具组合
```bash
# 先查产品，再问保修
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "MacBook Pro有哪些型号？保修政策是什么？"}'

# 预期：调用 product-search + knowledge-search 工具
```

#### 场景 3: 知识库增强（RAG）
```bash
# 技术问题
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "AirPods连接不上蓝牙怎么办？"}'

# 预期：调用 knowledge-search 工具检索技术支持库
```

### 6.2 编排模式测试

#### 测试 ReAct 模式（默认）

```bash
# 确保配置为 react
# application.yml: agent.orchestrator.default: react

curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询订单ORD001，如果已发货告诉我物流公司"}'

# 观察响应中的 steps 字段，应该看到：
# Step 1: Thought -> Action (order-query) -> Observation
# Step 2: Thought -> Final Answer
```

#### 测试 Plan and Execute 模式

修改 `application.yml`：

```yaml
agent:
  orchestrator:
    default: plan-execute
```

重启应用，然后测试：

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我想了解MacBook的型号、价格和保修政策"}'

# 观察日志，应该看到：
# Phase 1: Planning - 制定计划（3个步骤）
# Phase 2: Execution - 逐步执行
# Phase 3: Synthesis - 综合答案
```

### 6.3 会话管理测试

#### 多轮对话测试

```bash
# 第一轮
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我想买iPhone", "user_id": "test-user-001"}'

# 记录返回的 session_id

# 第二轮（使用相同 session_id）
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "有什么颜色？", "session_id": "<上一步的session_id>"}'

# 第三轮
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "价格多少？", "session_id": "<同一个session_id>"}'
```

#### 会话分析测试

```bash
# 查看所有会话
curl http://localhost:8080/api/sessions

# 查看用户的会话
curl http://localhost:8080/api/sessions/user/test-user-001

# 查看热门话题
curl http://localhost:8080/api/sessions/analytics/hot-topics?topN=5

# 查看统计信息
curl http://localhost:8080/api/sessions/stats
```

### 6.4 边界测试

#### 测试 1: 无效订单
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询订单ORD999"}'

# 预期：工具返回"订单不存在"，Agent给出友好回复
```

#### 测试 2: 无关问题
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "今天天气怎么样？"}'

# 预期：Agent判断无需工具，直接回复或引导用户
```

#### 测试 3: 复杂多步推理
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我之前买的订单ORD001里的产品，有没有类似的其他产品推荐？"}'

# 预期：
# Step 1: 查询订单ORD001（iPhone 15 Pro）
# Step 2: 搜索 iPhone 相关产品
# Step 3: 综合推荐
```

#### 测试 4: 最大迭代限制
```bash
# 修改配置降低迭代次数
# application.yml: agent.react.max-iterations: 2

# 提出复杂问题
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "对比订单ORD001和ORD002的产品，推荐类似产品，并告诉我保修政策"}'

# 预期：可能达到最大迭代，返回部分结果或提示
```

---

## 7. 性能测试

### 7.1 单请求性能测试

使用 `time` 命令：

```bash
time curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询订单ORD001"}'

# 关注 execution_time_ms 字段
# 典型值：1000-3000ms（取决于 LLM 响应速度）
```

### 7.2 并发测试

使用 Apache Bench (ab)：

```bash
# 安装 ab（如果没有）
# macOS: 自带
# Ubuntu: sudo apt install apache2-utils

# 并发测试：10个并发，共100个请求
ab -n 100 -c 10 -p data.json -T application/json \
  http://localhost:8080/api/chat

# data.json 内容：
echo '{"message":"查询订单ORD001"}' > data.json
```

预期结果：
```
Concurrency Level:      10
Time taken for tests:   25.123 seconds
Complete requests:      100
Failed requests:        0
Requests per second:    3.98 [#/sec]
Time per request:       2512.3 [ms] (mean)
```

### 7.3 压力测试

使用 wrk：

```bash
# 安装 wrk
# macOS: brew install wrk
# Ubuntu: sudo apt install wrk

# 压力测试：10个线程，100个连接，持续30秒
wrk -t10 -c100 -d30s -s post.lua http://localhost:8080/api/chat

# post.lua 内容：
# wrk.method = "POST"
# wrk.headers["Content-Type"] = "application/json"
# wrk.body = '{"message":"查询订单ORD001"}'
```

### 7.4 成本估算

基于 GPT-4 定价（2024年1月）：

| 场景 | Token 使用 | 成本（美元） |
|------|-----------|-------------|
| 简单查询（1次工具调用） | ~1500 tokens | ~$0.05 |
| 复杂查询（3次工具调用） | ~4000 tokens | ~$0.15 |
| 知识库检索（RAG） | ~2500 tokens | ~$0.08 |
| 100次测试请求 | ~150K tokens | ~$5-10 |

**节省成本建议**：
1. 开发测试使用 `gpt-3.5-turbo`（便宜10倍）
2. 降低 `max-iterations` 减少迭代
3. 减少 `max-tokens` 限制输出长度
4. 使用 Azure OpenAI 的包月套餐

---

## 8. 常见问题排查

### 8.1 启动问题

#### 问题 1: 端口被占用

```
Error: Port 8080 already in use
```

**解决方案**：

```bash
# 查找占用进程
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# 杀掉进程或更换端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

#### 问题 2: OpenAI API Key 无效

```
Error: Incorrect API key provided
```

**解决方案**：
1. 检查 API Key 格式（应以 `sk-` 开头）
2. 确认 API Key 有效且有余额
3. 检查环境变量是否正确设置
4. 访问 https://platform.openai.com/api-keys 验证

#### 问题 3: 依赖下载失败

```
Error: Could not resolve dependencies
```

**解决方案**：

```bash
# 清理 Maven 缓存
rm -rf ~/.m2/repository

# 重新下载
mvn clean install -U

# 如果仍失败，配置国内镜像
# 编辑 ~/.m2/settings.xml：
<mirror>
  <id>aliyun</id>
  <url>https://maven.aliyun.com/repository/public</url>
  <mirrorOf>central</mirrorOf>
</mirror>
```

### 8.2 运行时问题

#### 问题 1: 请求超时

```json
{
  "error": "Request timeout"
}
```

**原因**：LLM 响应慢或网络问题

**解决方案**：

```yaml
# 增加超时时间（application.yml）
spring:
  ai:
    openai:
      chat:
        options:
          timeout: 60000  # 60秒
```

#### 问题 2: 工具未找到

```
Tool not found: xxx
```

**排查步骤**：

```bash
# 查看日志，确认工具已注册
grep "Registering tool" logs/application.log

# 检查工具是否有 @Component 注解
# 检查 @PostConstruct 方法是否执行
```

#### 问题 3: 知识库为空

```
No documents found in knowledge base
```

**排查步骤**：

```yaml
# 确认配置启用
knowledge:
  enabled: true
  load-sample-data: true

# 查看日志
grep "知识库" logs/application.log

# 检查 SampleKnowledgeLoader 是否执行
```

#### 问题 4: 内存溢出

```
java.lang.OutOfMemoryError: Java heap space
```

**解决方案**：

```bash
# 增加堆内存
java -Xmx2g -Xms1g -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar

# 或使用 Maven
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

### 8.3 API 调用问题

#### 问题 1: 429 Too Many Requests

```json
{
  "error": {
    "message": "Rate limit exceeded",
    "type": "rate_limit_error"
  }
}
```

**解决方案**：
1. OpenAI 有速率限制（如每分钟 3 次）
2. 等待 1 分钟后重试
3. 升级到付费账户提高限额
4. 实现请求队列和重试机制

#### 问题 2: 401 Unauthorized

```json
{
  "error": "Invalid authentication"
}
```

**解决方案**：
1. 检查 API Key 是否正确
2. 确认 API Key 未过期
3. 检查账户余额是否充足

#### 问题 3: 响应不符合预期

Agent 没有调用正确的工具或给出错误答案。

**调试方法**：

```yaml
# 启用详细日志
logging:
  level:
    com.example.agentpattern: DEBUG

# 查看 Agent 推理过程
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你的问题"}' | jq '.steps'
```

**优化提示词**：
如果 Agent 经常做出错误判断，可以优化提示词模板：
- 编辑 `ReactPromptTemplate.java`
- 添加更多示例
- 明确工具使用规则

### 8.4 性能问题

#### 问题: 响应太慢（>10秒）

**排查步骤**：

1. **检查网络**：
```bash
curl -o /dev/null -s -w 'Time: %{time_total}s\n' https://api.openai.com
```

2. **检查 LLM 调用次数**：
```bash
# 查看响应中的 steps 数量
# 如果迭代过多，降低复杂度
```

3. **优化配置**：
```yaml
agent:
  react:
    max-iterations: 3  # 降低最大迭代

spring:
  ai:
    openai:
      chat:
        options:
          max-tokens: 1000  # 减少输出长度
```

4. **切换模型**：
```yaml
spring:
  ai:
    openai:
      model: gpt-3.5-turbo  # 更快但效果稍差
```

---

## 9. 生产部署建议

### 9.1 安全配置

```yaml
# 不要在代码中硬编码 API Key
# 使用环境变量或密钥管理服务

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # 从环境变量读取
```

### 9.2 监控和日志

```yaml
logging:
  file:
    name: logs/application.log
  level:
    root: INFO
    com.example.agentpattern: INFO
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### 9.3 持久化存储

生产环境建议使用数据库：

```yaml
# Redis 会话存储
spring:
  redis:
    host: localhost
    port: 6379

# MySQL 知识库存储
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_db
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

### 9.4 API 限流

```yaml
# 添加限流配置（需要集成 Spring Cloud Gateway 或 Resilience4j）
resilience4j:
  ratelimiter:
    instances:
      chatApi:
        limit-for-period: 10  # 每个周期允许10个请求
        limit-refresh-period: 60s  # 周期60秒
```

---

## 10. 下一步

### 学习建议

1. **理解 ReAct 模式**：阅读 `ARCHITECTURE.md` 中的数据流图
2. **查看日志**：观察 Agent 的推理过程
3. **修改提示词**：尝试优化 `ReactPromptTemplate.java`
4. **扩展工具**：参考 `OrderQueryTool.java` 实现新工具
5. **测试编排器**：对比 ReAct 和 Plan-Execute 的效果

### 进阶实验

1. 集成真实数据库（MySQL/PostgreSQL）
2. 接入向量数据库（Pinecone/Weaviate）
3. 实现 SSE 流式响应
4. 添加用户认证和授权
5. 实现分布式会话管理（Redis）
6. 添加监控和追踪（Prometheus + Grafana）

---

## 附录

### A. 完整环境变量列表

```bash
# OpenAI 配置
export OPENAI_API_KEY="sk-..."

# Azure OpenAI 配置（可选）
export AZURE_OPENAI_API_KEY="..."
export AZURE_OPENAI_ENDPOINT="https://..."
export AZURE_OPENAI_DEPLOYMENT_NAME="gpt-4"

# 应用配置
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=openai  # 或 azure-openai

# 日志级别
export LOGGING_LEVEL_ROOT=INFO
export LOGGING_LEVEL_AGENT=DEBUG
```

### B. 常用命令速查

```bash
# 编译
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 运行 JAR
java -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar

# 健康检查
curl http://localhost:8080/api/chat/health

# 快速测试
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"测试消息"}'

# 查看日志
tail -f logs/application.log

# 停止应用
# Ctrl+C（前台运行）
# kill $(lsof -t -i:8080)（后台运行）
```

### C. 项目资源

- **项目仓库**: `<your-repo-url>`
- **Spring AI 文档**: https://docs.spring.io/spring-ai/reference/
- **OpenAI API 文档**: https://platform.openai.com/docs
- **问题反馈**: `<your-issue-tracker>`

---

**祝测试顺利！如有问题，请查看日志或提交 Issue。**
