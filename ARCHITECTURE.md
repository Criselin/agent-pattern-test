# 系统架构和数据流

## 完整数据流图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用户请求                                         │
│                    POST /api/chat {"message": "..."}                         │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         1. Controller Layer                                  │
│                         ChatController                                       │
│                                                                              │
│  • 接收HTTP请求                                                              │
│  • 验证请求参数 (ChatRequest)                                                │
│  • 调用服务层                                                                │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         2. Service Layer                                     │
│                       CustomerServiceBot                                     │
│                                                                              │
│  • 获取或创建会话 (sessionId)                                                │
│  • 构建AgentContext (input, maxIterations, sessionId)                       │
│  • 调用Agent执行                                                             │
│  • 会话管理 (sessionStore: Map<String, AgentContext>)                       │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         3. Agent Layer                                       │
│                          ReactAgent                                          │
│                                                                              │
│  • 作为Agent接口的实现                                                       │
│  • 委托给OrchestratorRegistry                                                │
│  • 查找 "react" 编排器                                                       │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                      4. Orchestrator Registry                                │
│                      OrchestratorRegistry                                    │
│                                                                              │
│  • 管理所有注册的编排器                                                      │
│  • 根据名称返回编排器实例                                                    │
│  • orchestrators: Map<String, Orchestrator>                                 │
│    - "react" -> ReActOrchestrator                                           │
│    - "plan-execute" -> PlanAndExecuteOrchestrator                           │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                      5. Orchestrator Layer                                   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │              5a. ReActOrchestrator (默认)                             │  │
│  │                                                                       │  │
│  │  循环迭代 (最多5次):                                                  │  │
│  │  ┌────────────────────────────────────────────────────────┐         │  │
│  │  │ Step 1: 构建提示词                                      │         │  │
│  │  │  • 系统提示词 (ReactPromptTemplate)                    │         │  │
│  │  │  • 用户提示词 (input + scratchpad历史)                 │         │  │
│  │  └────────────────────────────────────────────────────────┘         │  │
│  │                          ↓                                           │  │
│  │  ┌────────────────────────────────────────────────────────┐         │  │
│  │  │ Step 2: 调用LLM                                         │         │  │
│  │  │  ChatModel.call(Prompt) -> Response                    │         │  │
│  │  │  返回: "Thought:...\nAction:...\nAction Input:..."     │         │  │
│  │  └────────────────────────────────────────────────────────┘         │  │
│  │                          ↓                                           │  │
│  │  ┌────────────────────────────────────────────────────────┐         │  │
│  │  │ Step 3: 解析LLM响应                                     │         │  │
│  │  │  • 正则提取 Thought                                     │         │  │
│  │  │  • 正则提取 Action                                      │         │  │
│  │  │  • 正则提取 Action Input                                │         │  │
│  │  │  • 检查 Final Answer                                    │         │  │
│  │  └────────────────────────────────────────────────────────┘         │  │
│  │                          ↓                                           │  │
│  │  ┌────────────────────────────────────────────────────────┐         │  │
│  │  │ Step 4: 执行工具 (如果没有Final Answer)                │         │  │
│  │  │  ToolRegistry.getTool(actionName)                      │         │  │
│  │  │    ↓                                                    │         │  │
│  │  │  Tool.execute(actionInput) -> ToolResult               │         │  │
│  │  └────────────────────────────────────────────────────────┘         │  │
│  │                          ↓                                           │  │
│  │  ┌────────────────────────────────────────────────────────┐         │  │
│  │  │ Step 5: 记录步骤到上下文                                │         │  │
│  │  │  AgentContext.addStep(AgentStep)                       │         │  │
│  │  │  - thought, action, actionInput, observation           │         │  │
│  │  └────────────────────────────────────────────────────────┘         │  │
│  │                          ↓                                           │  │
│  │                 有Final Answer? ──Yes→ 返回结果                      │  │
│  │                          │                                           │  │
│  │                          No                                          │  │
│  │                          ↓                                           │  │
│  │                  达到最大迭代? ──Yes→ 返回错误                        │  │
│  │                          │                                           │  │
│  │                          No                                          │  │
│  │                          ↓                                           │  │
│  │                    返回Step 1 (继续循环)                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │         5b. PlanAndExecuteOrchestrator (可选)                        │  │
│  │                                                                       │  │
│  │  Phase 1: Planning                                                   │  │
│  │    • LLM制定完整计划 -> Plan (List<PlanStep>)                        │  │
│  │                                                                       │  │
│  │  Phase 2: Execution                                                  │  │
│  │    • 遍历每个PlanStep                                                │  │
│  │    • 执行对应的Tool                                                  │  │
│  │    • 记录结果到step.result                                           │  │
│  │                                                                       │  │
│  │  Phase 3: Synthesis                                                  │  │
│  │    • LLM综合所有步骤结果                                             │  │
│  │    • 生成最终答案                                                    │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         6. Tool Layer                                        │
│                         ToolRegistry                                         │
│                                                                              │
│  • 管理所有注册的工具                                                        │
│  • tools: Map<String, Tool>                                                 │
│                                                                              │
│  工具实例:                                                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │
│  │ OrderQueryTool   │  │ ProductSearchTool│  │ FAQTool          │          │
│  │                  │  │                  │  │                  │          │
│  │ 订单数据库查询   │  │ 产品数据库查询   │  │ FAQ数据库查询    │          │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘          │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │ KnowledgeSearchTool                                          │          │
│  │                                                              │          │
│  │ • 解析JSON参数 (query, knowledge_base, top_k)                │          │
│  │ • 调用KnowledgeBaseRegistry                                  │          │
│  │ • 执行向量检索                                               │          │
│  │ • 格式化结果返回                                             │          │
│  └──────────────────┬───────────────────────────────────────────┘          │
│                     │                                                       │
└─────────────────────┼───────────────────────────────────────────────────────┘
                      │
                      ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                      7. Knowledge Base Layer                                 │
│                      KnowledgeBaseRegistry                                   │
│                                                                              │
│  • 管理所有知识库                                                            │
│  • knowledgeBases: Map<String, KnowledgeBase>                               │
│    - "product-manual" -> InMemoryVectorKnowledgeBase                        │
│    - "tech-support" -> InMemoryVectorKnowledgeBase                          │
│    - "company-policy" -> InMemoryVectorKnowledgeBase                        │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │ InMemoryVectorKnowledgeBase.search(query, topK)              │          │
│  │                                                              │          │
│  │  Step 1: 文本向量化                                          │          │
│  │    TextSimilarity.textToTfidfVector(query, corpus)          │          │
│  │                                                              │          │
│  │  Step 2: 计算相似度                                          │          │
│  │    • 余弦相似度 (cosineSimilarity)                           │          │
│  │    • BM25分数 (bm25Similarity)                               │          │
│  │    • 综合评分 = 0.6 * cosine + 0.4 * bm25                    │          │
│  │                                                              │          │
│  │  Step 3: 排序和TopK                                          │          │
│  │    • 按分数降序排序                                          │          │
│  │    • 返回前K个结果                                           │          │
│  │                                                              │          │
│  │  返回: SearchResult (List<ScoredDocument>)                   │          │
│  └──────────────────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         8. AI Model Layer                                    │
│                         ChatModel (Spring AI)                                │
│                                                                              │
│  • OpenAI GPT-4 / Azure OpenAI                                              │
│  • 接收: Prompt (SystemMessage + UserMessage)                               │
│  • 返回: ChatResponse (String content)                                      │
│  • 配置: temperature=0.7, max-tokens=2000                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         响应返回路径                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ↓
                    OrchestratorResult -> AgentResponse
                                 │
                                 ↓
                    CustomerServiceBot.chat() -> ChatResponse
                                 │
                                 ↓
                    ChatController -> HTTP 200 OK
                                 │
                                 ↓
                              用户客户端
```

## 组件交互序列图

### 场景1: 简单工具调用（ReAct模式）

```
用户 -> ChatController: POST /api/chat {"message": "查询订单ORD001"}
  │
  └─> CustomerServiceBot.chat(request)
        │
        ├─> 创建/获取 AgentContext (sessionId, input, maxIterations=5)
        │
        └─> ReactAgent.execute(context)
              │
              └─> OrchestratorRegistry.getOrchestrator("react")
                    │
                    └─> ReActOrchestrator.orchestrate(context)
                          │
                          ├─> [迭代1] ReactPromptTemplate.buildSystemPrompt(toolRegistry)
                          │             "Available tools: order-query, product-search..."
                          │
                          ├─> [迭代1] ChatModel.call(prompt)
                          │             LLM返回: "Thought: 需要查询订单\n
                          │                       Action: order-query\n
                          │                       Action Input: ORD001"
                          │
                          ├─> [迭代1] 解析LLM响应
                          │             thought = "需要查询订单"
                          │             action = "order-query"
                          │             actionInput = "ORD001"
                          │
                          ├─> [迭代1] ToolRegistry.getTool("order-query")
                          │             │
                          │             └─> OrderQueryTool.execute("ORD001")
                          │                   查询数据库 -> "订单信息: 已发货..."
                          │
                          ├─> [迭代1] context.addStep(AgentStep)
                          │
                          ├─> [迭代2] ChatModel.call(prompt_with_history)
                          │             LLM返回: "Thought: 已获取订单信息\n
                          │                       Final Answer: 您的订单已发货..."
                          │
                          └─> [迭代2] 检测到Final Answer，返回结果
                                OrchestratorResult.success(answer, 2, time)

                    返回路径:
                    OrchestratorResult -> AgentResponse -> ChatResponse -> HTTP响应
```

### 场景2: 知识库调用（ReAct + RAG）

```
用户: "iPhone无法开机怎么办？"

ChatController -> CustomerServiceBot -> ReactAgent -> ReActOrchestrator
  │
  ├─> [迭代1] LLM分析
  │     返回: "Thought: 这是技术问题，需要查询知识库\n
  │             Action: knowledge-search\n
  │             Action Input: {\"query\": \"iPhone无法开机\",
  │                           \"knowledge_base\": \"tech-support\"}"
  │
  ├─> [迭代1] ToolRegistry -> KnowledgeSearchTool.execute(input)
  │                              │
  │                              ├─> 解析JSON参数
  │                              │
  │                              └─> KnowledgeBaseRegistry.getKnowledgeBase("tech-support")
  │                                    │
  │                                    └─> InMemoryVectorKnowledgeBase.search("iPhone无法开机", 3)
  │                                          │
  │                                          ├─> TextSimilarity.textToTfidfVector(query)
  │                                          │
  │                                          ├─> 遍历所有文档计算相似度
  │                                          │     document1: 0.85 (iPhone无法开机问题)
  │                                          │     document2: 0.45 (MacBook性能问题)
  │                                          │     document3: 0.32 (AirPods连接问题)
  │                                          │
  │                                          └─> 排序并返回Top3
  │                                                SearchResult.formatForLLM()
  │                                                "找到1条相关信息:\n
  │                                                 【iPhone无法开机问题】\n
  │                                                 1. 强制重启...\n
  │                                                 2. 充电检查..."
  │
  ├─> [迭代2] LLM综合知识库信息
  │     提示词包含: 原问题 + 知识库检索结果
  │     返回: "Final Answer: 针对iPhone无法开机问题，建议:\n
  │             1. 强制重启: 快速按下音量加减键...\n
  │             2. 充电检查: 连接原装充电器..."
  │
  └─> 返回最终答案给用户
```

### 场景3: Plan and Execute模式

```
用户: "MacBook有哪些型号？价格如何？保修政策是什么？"

ChatController -> CustomerServiceBot -> ConfigurableAgent
  (假设配置为plan-execute编排器)
  │
  └─> PlanAndExecuteOrchestrator.orchestrate(context)
        │
        ├─> [Phase 1: Planning]
        │     ChatModel.call(PlannerPrompt)
        │     LLM返回:
        │       "Plan: 查询MacBook信息和保修政策
        │
        │        Step 1: 搜索MacBook产品信息
        │        Tool: product-search
        │        Input: MacBook
        │
        │        Step 2: 查询保修政策
        │        Tool: knowledge-search
        │        Input: {\"query\": \"保修政策\", \"knowledge_base\": \"company-policy\"}
        │
        │        Step 3: 综合答案
        │        Tool: none
        │        Input: 汇总以上信息"
        │
        ├─> parsePlan(llmResponse) -> Plan对象
        │     plan.steps = [
        │       PlanStep{stepNumber=1, tool="product-search", input="MacBook"},
        │       PlanStep{stepNumber=2, tool="knowledge-search", input="..."},
        │       PlanStep{stepNumber=3, tool="none"}
        │     ]
        │
        ├─> [Phase 2: Execution]
        │     For each step in plan.steps:
        │       │
        │       ├─> Step 1: ProductSearchTool.execute("MacBook")
        │       │     result = "MacBook Air 13: ¥9,499\nMacBook Pro 16: ¥19,999..."
        │       │     step1.status = COMPLETED
        │       │
        │       ├─> Step 2: KnowledgeSearchTool.execute(...)
        │       │     result = "保修政策: 1年有限保修..."
        │       │     step2.status = COMPLETED
        │       │
        │       └─> Step 3: 跳过 (tool="none")
        │             step3.status = COMPLETED
        │
        └─> [Phase 3: Synthesis]
              ChatModel.call(SynthesizerPrompt)
              提示词包含:
                - 原问题
                - 所有步骤的执行结果
              LLM返回:
                "MacBook系列包括MacBook Air和MacBook Pro。
                 MacBook Air 13英寸售价¥9,499，MacBook Pro 16英寸售价¥19,999。
                 保修政策：所有产品享受1年有限保修..."

              返回 OrchestratorResult.success(answer)
```

## 数据结构流转

### AgentContext (贯穿整个流程)

```java
AgentContext {
    input: "用户问题",
    sessionId: "session-xxx",
    maxIterations: 5,
    currentIteration: 2,
    steps: [
        AgentStep {
            thought: "需要查询订单",
            action: "order-query",
            actionInput: "ORD001",
            observation: "订单信息: ...",
            timestamp: 1234567890
        },
        AgentStep {
            thought: "已获取信息",
            action: null,
            actionInput: null,
            observation: null,
            timestamp: 1234567891
        }
    ],
    variables: {
        "orchestrator": "react",
        "custom_data": "..."
    }
}
```

### 工具执行流程

```
ToolRegistry.getTool(name)
    ↓
Tool.execute(input)
    ↓
Tool.ToolResult {
    success: true,
    output: "执行结果",
    error: null
}
```

### 知识库检索流程

```
KnowledgeSearchTool.execute(input)
    ↓
KnowledgeBaseRegistry.getKnowledgeBase(name)
    ↓
InMemoryVectorKnowledgeBase.search(query, topK)
    ↓
SearchResult {
    query: "查询文本",
    documents: [
        ScoredDocument {
            document: Document {
                id: "doc-1",
                title: "标题",
                content: "内容...",
                embedding: [0.1, 0.2, ...],
                metadata: {...}
            },
            score: 0.85,
            rank: 1
        }
    ],
    searchTimeMs: 45,
    knowledgeBaseName: "tech-support"
}
    ↓
formatForLLM() -> String (格式化的文本)
```

## 关键配置点

### 1. 编排器选择
```yaml
agent:
  orchestrator:
    default: react  # 决定使用哪个编排器
```

### 2. 会话管理
```java
// CustomerServiceBot
Map<String, AgentContext> sessionStore
// Key: sessionId
// Value: AgentContext (保存历史对话)
```

### 3. 工具注册
```java
// 启动时自动注册
@PostConstruct
public void register() {
    toolRegistry.registerTool(this);
}
```

### 4. 知识库加载
```java
// SampleKnowledgeLoader
@PostConstruct
public void loadSampleData() {
    // 加载product-manual
    // 加载tech-support
    // 加载company-policy
}
```

## 性能考虑

### LLM调用次数

**ReAct模式** (最坏情况):
- 迭代1: 1次 (Thought -> Action)
- 迭代2: 1次 (Thought -> Action)
- 迭代3: 1次 (Thought -> Action)
- 迭代4: 1次 (Thought -> Action)
- 迭代5: 1次 (Final Answer)
- **总计: 最多5次**

**Plan and Execute模式**:
- Planning: 1次
- Execution: 0次 (直接执行工具)
- Synthesis: 1次
- **总计: 2次**

### 缓存策略

1. **会话缓存**: sessionStore (内存)
2. **向量缓存**: Document.embedding (首次计算后缓存)
3. **语料库缓存**: InMemoryVectorKnowledgeBase.corpus

### 并发处理

- 所有Registry使用 `ConcurrentHashMap`
- 支持多线程并发请求
- 每个会话独立的AgentContext

## 扩展点

1. **新增编排器**: 实现 `Orchestrator` 接口
2. **新增工具**: 实现 `Tool` 接口
3. **新增知识库**: 实现 `KnowledgeBase` 接口
4. **自定义Agent**: 实现 `Agent` 接口
