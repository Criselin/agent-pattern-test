# Agent Pattern Test

一个基于Spring AI框架构建的Agent设计模式测试项目，实现了ReAct Agent模式的智能客服机器人。

## 项目简介

本项目旨在探索和测试各种Agent设计模式，当前已实现：

- **编排器抽象层**: 可扩展的Agent编排架构，支持多种编排模式
- **ReAct编排模式**: Reasoning and Acting的循环执行模式
- **Plan and Execute编排模式**: 先制定计划后逐步执行的结构化模式
- **工具系统**: 订单查询、产品搜索、FAQ等客服工具
- **知识库系统**: 支持向量检索的RAG知识库，Agent可动态决定是否调用
- **会话管理系统**: 完整的会话生命周期管理和数据分析功能
- **智能客服机器人**: 支持多种编排模式的客服应用，集成知识库增强
- **多品牌产品支持**: 支持 Apple、Reolink 等多品牌产品咨询和推荐（可扩展）

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
│   │   │   ├── AgentContext.java       # Agent上下文
│   │   │   └── ConfigurableAgent.java  # 可配置Agent
│   │   ├── orchestrator/                # 编排器模块
│   │   │   ├── core/                    # 编排器抽象层
│   │   │   │   ├── Orchestrator.java   # 编排器接口
│   │   │   │   ├── OrchestratorResult.java # 编排结果
│   │   │   │   └── OrchestratorRegistry.java # 编排器注册表
│   │   │   ├── react/                   # ReAct编排器
│   │   │   │   └── ReActOrchestrator.java
│   │   │   └── planexecute/             # Plan and Execute编排器
│   │   │       ├── Plan.java            # 计划模型
│   │   │       ├── PlanAndExecutePromptTemplate.java
│   │   │       └── PlanAndExecuteOrchestrator.java
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
│   ├── session/                         # 会话管理模块
│   │   ├── model/                       # 会话模型
│   │   │   └── Session.java            # 会话实体
│   │   ├── manager/                     # 会话管理器
│   │   │   ├── SessionManager.java     # 管理器接口
│   │   │   └── DefaultSessionManager.java # 默认实现
│   │   ├── repository/                  # 会话存储
│   │   │   ├── SessionRepository.java  # 存储接口
│   │   │   └── InMemorySessionRepository.java # 内存存储
│   │   ├── analytics/                   # 会话分析
│   │   │   └── SessionAnalytics.java   # 统计分析服务
│   │   └── controller/                  # 会话API
│   │       └── SessionController.java  # REST接口
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

## 多品牌产品支持

系统现已支持**多品牌产品**的智能咨询和推荐，采用可扩展的架构设计。

### 已支持品牌

#### 1. Apple 产品（9款）
- iPhone 15 Pro、iPhone 15
- MacBook Pro 16、MacBook Air 13
- iPad Pro 12.9、iPad Air
- AirPods Pro、AirPods Max
- Apple Watch Series 9

#### 2. Reolink 安防产品（8款）
- **无线摄像头**: Argus 4 Pro（4K双镜头，ColorX夜视，太阳能）
- **有线摄像头**: RLC-810A（4K PoE，AI检测）
- **室内云台**: E1 Zoom（5MP，3倍变焦，智能追踪）
- **双目摄像头**: Duo 2 WiFi（180°全景，ColorX夜视）
- **追踪摄像头**: TrackMix WiFi（广角+长焦，智能追踪）
- **监控套装**: RLK8-800B4（8路NVR+4摄像头+2TB硬盘）
- **智能门铃**: Video Doorbell（5MP，人形检测，双向对讲）
- **泛光灯摄像头**: Lumus（内置泛光灯+警报器）

### Reolink 产品特色

#### 支持的咨询类型

1. **产品搜索**
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "有哪些 Reolink 摄像头？"}'
   ```

2. **技术问题**
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Reolink 摄像头连接不上 WiFi 怎么办？"}'
   ```

3. **方案设计**
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "我有一个小商铺，想装监控，预算5000元"}'
   ```

#### Reolink 知识库

系统包含 **3个 Reolink 专属知识库**：

| 知识库 | 内容 | 文档数 |
|--------|------|-------|
| `reolink-product-manual` | 产品手册、功能说明、技术参数 | 4+ |
| `reolink-tech-support` | 常见问题、故障排查、优化方案 | 3+ |
| `reolink-installation-guide` | 安装指南、方案设计、最佳实践 | 2+ |

#### 测试 Reolink 功能

运行自动化测试脚本：

```bash
# 确保应用正在运行
mvn spring-boot:run

# 在另一个终端运行测试
./test-reolink.sh
```

测试脚本包含 10 个测试场景：
- ✓ Reolink 品牌搜索
- ✓ 摄像头类别搜索
- ✓ 具体产品咨询
- ✓ 4K 摄像头搜索
- ✓ 价格查询
- ✓ 监控套装咨询
- ✓ 技术问题咨询
- ✓ WiFi 连接问题
- ✓ PoE 摄像头咨询
- ✓ 混合品牌搜索

### 扩展新品牌

添加新品牌只需 **3步**，无需修改核心代码：

#### 步骤 1: 创建产品配置文件

在 `src/main/resources/data/products/` 创建 JSON 文件：

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

#### 步骤 2: 创建知识库加载器（可选）

```java
@Component
public class NewBrandKnowledgeLoader {
    private final KnowledgeBaseRegistry registry;

    @PostConstruct
    public void loadKnowledge() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
            "brand-manual",
            "品牌产品手册"
        );

        List<Document> docs = // 准备文档...
        kb.addDocuments(docs);
        registry.registerKnowledgeBase(kb);
    }
}
```

#### 步骤 3: 更新配置

```yaml
products:
  supported-brands:
    - Apple
    - Reolink
    - YourBrand  # 添加新品牌
```

**完成！** 重启应用即可使用新品牌。

详细说明请查看 **[REOLINK_INTEGRATION.md](REOLINK_INTEGRATION.md)**

### 知识库特性

- **向量检索**: 基于TF-IDF的语义搜索
- **多知识库**: 支持同时维护多个独立知识库
- **动态加载**: 运行时动态添加/更新知识库
- **相似度算法**: 支持余弦相似度、BM25、Jaccard等
- **可扩展**: 易于集成其他向量数据库（如Pinecone、Weaviate等）

## 会话管理系统

### 架构设计

会话管理系统提供完整的对话生命周期管理和分析功能：

- **Session模型**: 完整的会话状态跟踪
- **SessionManager**: 会话生命周期管理
- **SessionRepository**: 可扩展的存储抽象层
- **SessionAnalytics**: 会话数据分析和统计

### 核心功能

#### 1. 会话管理

- **会话创建**: 自动生成唯一会话ID
- **会话状态**: ACTIVE（活跃）、INACTIVE（非活跃）、EXPIRED（过期）、CLOSED（已关闭）
- **自动过期**: 支持TTL（默认60分钟）
- **定时清理**: 每小时自动清理过期会话

#### 2. 消息历史

每个会话保存完整的消息历史：

- **用户消息**: 记录用户输入
- **助手消息**: 记录机器人回复及执行细节
  - 执行时间（毫秒）
  - 使用的工具列表
  - 推理步骤数量
  - 成功/失败状态
  - 错误信息（如有）

#### 3. 会话分析

提供四大类分析功能：

**整体统计**:
- 总会话数、活跃会话数、非活跃会话数、过期会话数
- 平均会话时长
- 平均每会话消息数
- 唯一用户数

**用户行为分析**:
- 用户的总会话数和消息数
- 最常用的编排器类型
- 用户标签统计
- 最后活跃时间

**热门话题分析**:
- 提取对话中的高频关键词
- 统计话题热度
- 支持自定义TopN

**时间范围统计**:
- 指定时间段内的会话数和消息数
- 每小时会话分布

### API接口

#### 会话管理

```bash
# 获取所有会话
GET /api/sessions

# 获取指定会话详情
GET /api/sessions/{sessionId}

# 获取用户的所有会话
GET /api/sessions/user/{userId}

# 根据状态查询会话
GET /api/sessions/status/{status}

# 创建新会话
POST /api/sessions?userId=user123&ttlMinutes=120

# 更新会话状态
PUT /api/sessions/{sessionId}/status?status=CLOSED

# 删除会话
DELETE /api/sessions/{sessionId}

# 清空所有会话
DELETE /api/sessions

# 手动触发清理
POST /api/sessions/cleanup

# 检查会话是否存在
GET /api/sessions/{sessionId}/exists

# 获取活跃会话数量
GET /api/sessions/count/active

# 获取总会话数量
GET /api/sessions/count/total
```

#### 分析统计

```bash
# 获取整体统计
GET /api/sessions/stats

# 获取用户行为分析
GET /api/sessions/analytics/user/{userId}

# 获取热门话题（Top 10）
GET /api/sessions/analytics/hot-topics?topN=10

# 获取时间范围统计
GET /api/sessions/analytics/time-range?startTime=2024-01-01T00:00:00&endTime=2024-01-31T23:59:59
```

### 配置说明

在 `application.yml` 中配置会话管理：

```yaml
session:
  default-ttl-minutes: 60      # 会话默认过期时间（分钟）
  auto-cleanup: true           # 是否自动清理过期会话
  cleanup-cron: "0 0 * * * ?"  # 清理任务cron表达式（每小时）
```

### 存储扩展

当前实现使用内存存储（`InMemorySessionRepository`），可轻松扩展到：

#### Redis存储

```java
@Component
public class RedisSessionRepository implements SessionRepository {
    private final RedisTemplate<String, Session> redisTemplate;

    @Override
    public Session save(Session session) {
        redisTemplate.opsForValue().set(
            "session:" + session.getSessionId(),
            session,
            session.getExpiresAt().atZone(ZoneId.systemDefault()).toEpochSecond(),
            TimeUnit.SECONDS
        );
        return session;
    }
    // ... 其他方法实现
}
```

#### MySQL存储

```java
@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    private String sessionId;

    @Column(columnDefinition = "json")
    private String messagesJson;

    // ... JPA映射
}

@Repository
public interface JpaSessionRepository extends JpaRepository<SessionEntity, String> {
    List<SessionEntity> findByUserId(String userId);
    List<SessionEntity> findByStatus(SessionStatus status);
}
```

### 会话特性

- **完整历史**: 保存所有对话消息和执行细节
- **性能指标**: 跟踪每次交互的执行时间和工具使用
- **灵活查询**: 按用户、状态、时间范围查询会话
- **自动管理**: 自动过期和定时清理
- **可扩展**: 支持内存、Redis、MySQL等多种存储
- **数据分析**: 丰富的统计分析功能

### 使用示例

#### 查询用户会话历史

```bash
curl http://localhost:8080/api/sessions/user/user123
```

**响应**:
```json
[
  {
    "sessionId": "session-abc123",
    "userId": "user123",
    "status": "ACTIVE",
    "messages": [
      {
        "messageId": "msg-001",
        "role": "USER",
        "content": "查询订单ORD001",
        "timestamp": "2024-01-20T10:00:00"
      },
      {
        "messageId": "msg-002",
        "role": "ASSISTANT",
        "content": "您的订单ORD001已发货...",
        "timestamp": "2024-01-20T10:00:02",
        "executionTimeMs": 1234,
        "toolsUsed": ["order-query"],
        "stepCount": 2,
        "success": true
      }
    ],
    "totalMessages": 2,
    "createdAt": "2024-01-20T10:00:00",
    "lastAccessedAt": "2024-01-20T10:00:02"
  }
]
```

#### 获取热门话题

```bash
curl http://localhost:8080/api/sessions/analytics/hot-topics?topN=5
```

**响应**:
```json
{
  "totalMessages": 150,
  "uniqueKeywords": 45,
  "topKeywords": {
    "订单": 35,
    "iPhone": 28,
    "退货": 18,
    "配送": 15,
    "保修": 12
  }
}
```

## Agent编排系统

### 设计理念

本项目实现了Agent编排抽象层，将Agent的"思考方式"（编排策略）与"执行能力"（工具调用）分离，使得可以轻松切换和扩展不同的编排模式。

### 架构

```
Agent (应用层)
    ↓
Orchestrator (编排层) - 定义思考和决策流程
    ↓
Tools (执行层) - 实际执行操作
```

### 可用编排模式

#### 1. ReAct (Reasoning and Acting)

**特点**: 思考-行动-观察的循环迭代

**工作流程**:
```
1. Thought（思考）: 分析当前情况
2. Action（行动）: 选择工具执行
3. Observation（观察）: 获取执行结果
4. 重复直到找到答案
```

**适用场景**:
- 需要多步推理的问题
- 每步依赖前一步结果
- 动态决策路径

**示例**:
```
Question: "查询订单ORD001并告诉我什么时候发货"

Thought: 用户想知道订单发货时间，需要先查询订单
Action: order-query
Action Input: ORD001
Observation: [订单信息: 已发货, 2024-01-15]

Thought: 已获取订单信息，可以回答了
Final Answer: 您的订单ORD001已于2024-01-15发货
```

#### 2. Plan and Execute

**特点**: 先制定完整计划，再逐步执行

**工作流程**:
```
阶段1 - 规划（Planning）:
  分析问题 → 制定完整计划 → 列出所有步骤

阶段2 - 执行（Execution）:
  按顺序执行每个步骤 → 收集结果

阶段3 - 综合（Synthesis）:
  汇总所有结果 → 生成最终答案
```

**适用场景**:
- 可以预先规划的任务
- 需要执行多个独立步骤
- 步骤顺序明确

**示例**:
```
Question: "MacBook有什么型号？价格如何？保修政策是什么？"

Plan:
  Step 1: 搜索MacBook产品信息
  Step 2: 查询保修政策信息
  Step 3: 综合答案

Execution:
  Step 1 Result: [MacBook Air 13: ¥9,499, MacBook Pro 16: ¥19,999...]
  Step 2 Result: [保修1年，支持AppleCare+延保...]

Final Answer: MacBook系列包括...价格从¥9,499起...保修政策为...
```

### 配置编排器

在`application.yml`中配置默认编排器:

```yaml
agent:
  orchestrator:
    default: react  # 或 plan-execute
```

### 使用编排器

#### 方式1: 使用默认编排器（现有API不变）

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你的问题"}'
```

#### 方式2: 指定编排器（未来支持）

通过上下文变量指定：
```java
AgentContext context = AgentContext.builder()
    .input("你的问题")
    .build();
context.setVariable("orchestrator", "plan-execute");
```

### 编排器对比

| 特性 | ReAct | Plan and Execute |
|------|-------|------------------|
| 思考方式 | 逐步迭代 | 预先规划 |
| 适应性 | 高，可动态调整 | 中，按计划执行 |
| 可预测性 | 低 | 高 |
| LLM调用次数 | 多（每步一次） | 少（规划+执行+综合） |
| 最适场景 | 复杂推理 | 结构化任务 |
| 步骤透明度 | 高（每步可见） | 中（计划可见） |

## 扩展新的编排模式

1. 实现 `Orchestrator` 接口
2. 定义编排逻辑
3. 注册为Spring Bean

示例：

```java
@Component
public class MyCustomOrchestrator implements Orchestrator {
    @Override
    public String getName() {
        return "my-orchestrator";
    }

    @Override
    public OrchestratorType getType() {
        return OrchestratorType.CUSTOM;
    }

    @Override
    public OrchestratorResult orchestrate(AgentContext context) {
        // 实现你的编排逻辑
        // 1. 制定策略
        // 2. 调用工具
        // 3. 处理结果
        return OrchestratorResult.success(answer, steps, time, getName());
    }
}
```

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
