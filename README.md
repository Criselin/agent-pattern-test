# Agent Pattern Test

一个基于Spring AI框架构建的Agent设计模式测试项目，实现了ReAct Agent模式的智能客服机器人。

## 项目简介

本项目旨在探索和测试各种Agent设计模式，当前已实现：

- **ReAct Agent模式**: Reasoning and Acting的循环执行模式
- **工具系统**: 订单查询、产品搜索、FAQ等客服工具
- **知识库系统**: 支持向量检索的RAG知识库，Agent可动态决定是否调用
- **智能客服机器人**: 基于ReAct Agent的客服应用，集成知识库增强

## 技术栈

- **框架**: Spring Boot 3.2.0
- **AI**: Spring AI (支持OpenAI和Azure OpenAI)
- **语言**: Java 17
- **构建工具**: Maven
- **日志**: Lombok + SLF4J

## 项目结构

```
agent-pattern-test/
├── src/main/java/com/example/agentpattern/
│   ├── AgentPatternApplication.java    # 主应用类
│   ├── agent/                           # Agent核心模块
│   │   ├── core/                        # Agent抽象层
│   │   │   ├── Agent.java              # Agent接口
│   │   │   └── AgentContext.java       # Agent上下文
│   │   ├── react/                       # ReAct Agent实现
│   │   │   ├── ReactAgent.java         # ReAct Agent
│   │   │   └── ReactPromptTemplate.java # 提示词模板
│   │   └── tool/                        # 工具系统
│   │       ├── Tool.java               # 工具接口
│   │       └── ToolRegistry.java       # 工具注册表
│   ├── chatbot/                         # 客服机器人
│   │   ├── controller/                  # REST API
│   │   ├── model/                       # 数据模型
│   │   └── service/                     # 业务逻辑
│   ├── config/                          # 配置类
│   ├── knowledge/                       # 知识库模块
│   │   ├── base/                        # 知识库抽象层
│   │   │   ├── Document.java           # 文档模型
│   │   │   ├── KnowledgeBase.java      # 知识库接口
│   │   │   ├── KnowledgeBaseRegistry.java # 知识库注册表
│   │   │   └── SearchResult.java       # 搜索结果
│   │   ├── vector/                      # 向量知识库
│   │   │   ├── TextSimilarity.java     # 文本相似度计算
│   │   │   └── InMemoryVectorKnowledgeBase.java # 内存向量库
│   │   └── loader/                      # 数据加载器
│   │       └── SampleKnowledgeLoader.java # 示例数据加载
│   └── tools/                           # 具体工具实现
│       ├── OrderQueryTool.java         # 订单查询
│       ├── ProductSearchTool.java      # 产品搜索
│       ├── FAQTool.java                # 常见问题
│       └── KnowledgeSearchTool.java    # 知识库搜索
└── src/main/resources/
    ├── application.yml                  # OpenAI配置
    └── application-azure-openai.yml     # Azure OpenAI配置
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- OpenAI API Key 或 Azure OpenAI 访问权限

### 2. 配置

#### 使用OpenAI（默认）

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: your-openai-api-key
      model: gpt-4
```

或通过环境变量：

```bash
export OPENAI_API_KEY=your-openai-api-key
```

#### 使用Azure OpenAI

编辑 `src/main/resources/application-azure-openai.yml`：

```yaml
spring:
  ai:
    azure:
      openai:
        api-key: your-azure-openai-api-key
        endpoint: https://your-resource.openai.azure.com
        deployment-name: gpt-4
```

或通过环境变量：

```bash
export AZURE_OPENAI_API_KEY=your-api-key
export AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
export AZURE_OPENAI_DEPLOYMENT_NAME=gpt-4
```

启动时指定profile：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=azure-openai
```

### 3. 编译运行

```bash
# 编译
mvn clean package

# 运行（使用OpenAI）
mvn spring-boot:run

# 运行（使用Azure OpenAI）
mvn spring-boot:run -Dspring-boot.run.profiles=azure-openai
```

### 4. 测试API

应用启动后访问: http://localhost:8080

#### 获取欢迎消息

```bash
curl http://localhost:8080/api/chat/welcome
```

#### 发送聊天消息

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我查询订单ORD001的物流信息"
  }'
```

#### 查询产品

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我想买iPhone，有什么推荐吗？",
    "session_id": "test-session-123"
  }'
```

#### 常见问题解答

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请问你们的退货政策是什么？"
  }'
```

#### 健康检查

```bash
curl http://localhost:8080/api/chat/health
```

#### 统计信息

```bash
curl http://localhost:8080/api/chat/stats
```

## API接口说明

### POST /api/chat

聊天接口（同步响应）

**请求体**:
```json
{
  "message": "用户消息",
  "session_id": "可选的会话ID",
  "user_id": "可选的用户ID"
}
```

**响应体**:
```json
{
  "message": "机器人回复",
  "session_id": "会话ID",
  "success": true,
  "execution_time_ms": 1234,
  "steps": [
    {
      "thought": "Agent的思考过程",
      "action": "使用的工具名称",
      "action_input": "工具输入",
      "observation": "工具执行结果"
    }
  ]
}
```

### GET /api/chat/welcome

获取欢迎消息

### DELETE /api/chat/session/{sessionId}

清除指定会话

### GET /api/chat/stats

获取系统统计信息

### GET /api/chat/health

健康检查

## ReAct Agent工作原理

ReAct (Reasoning and Acting) Agent通过以下循环来解决问题：

1. **Thought（思考）**: Agent分析当前情况并决定下一步行动
2. **Action（行动）**: 选择并执行一个工具
3. **Observation（观察）**: 获取工具执行结果
4. **重复**: 直到找到最终答案或达到最大迭代次数

### 示例对话流程

用户: "帮我查询订单ORD001的物流信息"

```
Thought: 用户想查询订单ORD001的物流信息，我需要使用订单查询工具
Action: order-query
Action Input: ORD001
Observation: 订单信息:
订单号: ORD001
商品: iPhone 15 Pro
状态: 已发货
下单时间: 2024-01-15
物流信息: 顺丰快递: SF1234567890

Thought: 我已经获取到了订单的物流信息，可以回答用户了
Final Answer: 您的订单ORD001（iPhone 15 Pro）已发货，物流信息如下：
- 物流公司: 顺丰快递
- 快递单号: SF1234567890
- 下单时间: 2024-01-15
```

## 可用工具

### 1. order-query (订单查询)

查询订单信息和物流状态

**输入**: 订单号（如: ORD001）

**测试订单**:
- ORD001: 已发货
- ORD002: 配送中
- ORD003: 已签收
- ORD004: 处理中

### 2. product-search (产品搜索)

搜索产品信息

**输入**: 产品名称或类别（如: iPhone, 耳机, MacBook）

**可用产品**: iPhone系列、MacBook系列、iPad系列、AirPods系列、Apple Watch等

### 3. faq (常见问题)

回答常见问题

**输入**: 问题关键词（如: 退货, 配送, 支付, 保修, 发票, 换货）

### 4. knowledge-search (知识库搜索)

从知识库中检索相关信息（RAG）

**输入**: JSON格式或纯文本
```json
{
  "query": "iPhone电池保养",
  "knowledge_base": "product-manual",  // 可选
  "top_k": 3  // 可选，默认3
}
```

**可用知识库**:
- `product-manual`: 产品使用手册和规格说明
- `tech-support`: 常见技术问题解决方案
- `company-policy`: 公司服务政策和保修条款

## 知识库系统

### 架构设计

知识库系统采用可扩展的架构，支持多种知识库类型：

- **抽象层**: `KnowledgeBase`接口定义标准操作
- **向量检索**: 基于TF-IDF和余弦相似度的文本检索
- **文本相似度**: 支持余弦相似度、BM25、Jaccard等算法
- **动态装配**: Agent可根据需要动态决定是否调用知识库

### RAG工作流程

1. **用户提问**: "iPhone电池如何保养？"
2. **Agent决策**: Agent分析问题，决定使用knowledge-search工具
3. **向量检索**: 在知识库中检索相关文档
4. **上下文增强**: 将检索到的知识补充到上下文
5. **生成回答**: LLM基于增强的上下文生成答案

### 示例对话

用户: "MacBook运行很慢，怎么办？"

```
Thought: 用户询问Mac性能问题，我应该先搜索技术支持知识库
Action: knowledge-search
Action Input: {"query": "Mac运行缓慢", "knowledge_base": "tech-support"}
Observation: 从知识库 'tech-support' 检索到的信息:
【Mac运行缓慢优化】
1. 检查存储空间...
2. 关闭开机启动项...
...

Thought: 我已获取到详细的优化方法，可以给出全面的建议
Final Answer: 针对您的MacBook运行缓慢问题，建议您尝试以下优化方法：
1. 检查存储空间是否充足...
```

### 添加自定义知识库

```java
@Component
public class CustomKnowledgeLoader {
    private final KnowledgeBaseRegistry registry;

    @PostConstruct
    public void loadCustomKnowledge() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "my-knowledge",
                "我的自定义知识库"
        );

        List<Document> docs = Arrays.asList(
                Document.builder()
                        .id(UUID.randomUUID().toString())
                        .title("文档标题")
                        .content("文档内容...")
                        .source("来源")
                        .build()
        );

        kb.addDocuments(docs);
        registry.registerKnowledgeBase(kb);
    }
}
```

### 知识库特性

- **向量检索**: 基于TF-IDF的语义搜索
- **多知识库**: 支持同时维护多个独立知识库
- **动态加载**: 运行时动态添加/更新知识库
- **相似度算法**: 支持余弦相似度、BM25、Jaccard等
- **可扩展**: 易于集成其他向量数据库（如Pinecone、Weaviate等）

## 扩展新的Agent模式

本项目设计为可扩展的Agent框架，可以轻松添加新的Agent模式：

1. 实现 `Agent` 接口
2. 定义自己的执行逻辑
3. 使用 `ToolRegistry` 访问工具
4. 注册为Spring Bean

示例：

```java
@Component
public class MyCustomAgent implements Agent {
    @Override
    public AgentResponse execute(AgentContext context) {
        // 实现你的Agent逻辑
    }
}
```

## 添加新工具

1. 实现 `Tool` 接口
2. 在 `@PostConstruct` 中注册到 `ToolRegistry`
3. 实现 `execute()` 方法

示例：

```java
@Component
public class MyTool implements Tool {
    private final ToolRegistry toolRegistry;

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
    }

    @Override
    public ToolResult execute(String input) {
        // 实现工具逻辑
    }
}
```

## 配置说明

### Agent配置

```yaml
agent:
  react:
    max-iterations: 5      # 最大迭代次数
    enable-logging: true   # 启用日志
```

### 客服机器人配置

```yaml
chatbot:
  name: "智能客服助手"
  welcome-message: "您好！我是智能客服助手..."
  context-window: 10     # 保留的对话轮次
```

## 未来计划

- [ ] 实现SSE流式响应接口
- [ ] 添加更多Agent模式（Plan-and-Execute, Self-Ask等）
- [ ] 集成向量数据库支持RAG
- [ ] 添加多轮对话记忆管理
- [ ] 支持更多LLM提供商
- [ ] 添加Agent性能监控和追踪
- [ ] 实现工具调用的并行执行

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！
