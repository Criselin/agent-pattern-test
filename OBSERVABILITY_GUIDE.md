# Langfuse + OpenTelemetry 观测指南

本项目已集成 **Langfuse** 和 **OpenTelemetry** 进行全链路追踪，可以观测到每轮对话的所有流程。

## 目录

- [架构概览](#架构概览)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [追踪数据](#追踪数据)
- [可视化观测](#可视化观测)
- [故障排查](#故障排查)

---

## 架构概览

### 追踪层次结构

```
HTTP Request (根 Trace)
└─ conversation (对话 Span)
   └─ orchestrator.react (编排器 Span)
      ├─ Iteration 1
      │  ├─ llm.call (LLM Generation)
      │  │  ├─ model: gpt-4
      │  │  ├─ prompt: "..."
      │  │  ├─ completion: "..."
      │  │  └─ usage: {promptTokens, completionTokens, totalTokens}
      │  └─ tool.{toolName} (工具调用 Span)
      │     ├─ input: "..."
      │     └─ output: "..."
      ├─ Iteration 2
      │  └─ llm.call
      └─ ...
```

### 技术栈

1. **OpenTelemetry**
   - 提供标准的分布式追踪能力
   - 记录所有 span 和 trace 信息
   - 导出到 OTLP 兼容的后端（Jaeger、Tempo 等）

2. **Langfuse**
   - 专注于 LLM 应用的观测
   - 记录 LLM 调用的详细信息（prompt、completion、tokens）
   - 提供专门的 LLM 可视化界面

---

## 快速开始

### 1. 配置 Langfuse

首先需要在 [Langfuse Cloud](https://cloud.langfuse.com) 或自建实例创建项目，获取 API 密钥。

设置环境变量：

```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
```

或者在 `application.yml` 中配置：

```yaml
observability:
  langfuse:
    enabled: true
    host: https://cloud.langfuse.com
    public-key: pk-lf-...
    secret-key: sk-lf-...
```

### 2. 配置 OpenTelemetry

#### 方案 A: 使用 Jaeger (推荐用于本地开发)

启动 Jaeger (支持 OTLP):

```bash
docker run -d --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest
```

访问 Jaeger UI: http://localhost:16686

#### 方案 B: 使用 Grafana Tempo

```yaml
# docker-compose.yml
version: '3'
services:
  tempo:
    image: grafana/tempo:latest
    ports:
      - "4317:4317"  # OTLP gRPC
      - "4318:4318"  # OTLP HTTP
    volumes:
      - ./tempo-config.yaml:/etc/tempo.yaml
    command: ["-config.file=/etc/tempo.yaml"]
```

### 3. 启动应用

```bash
# 编译项目
mvn clean package

# 运行应用
java -jar target/agent-pattern-test-1.0.0-SNAPSHOT.jar
```

### 4. 发送测试请求

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我想查询订单ORD001的状态",
    "userId": "test-user"
  }'
```

---

## 配置说明

### application.yml 配置项

```yaml
observability:
  # OpenTelemetry 配置
  otel:
    enabled: true                        # 是否启用 OTel 追踪
    service-name: agent-pattern-test    # 服务名称（用于标识追踪来源）
    endpoint: http://localhost:4317     # OTLP gRPC endpoint

  # Langfuse 配置
  langfuse:
    enabled: true                        # 是否启用 Langfuse
    host: https://cloud.langfuse.com    # Langfuse 服务地址
    public-key: ${LANGFUSE_PUBLIC_KEY}  # Public Key
    secret-key: ${LANGFUSE_SECRET_KEY}  # Secret Key
```

### 禁用追踪

如果不需要追踪，可以设置：

```yaml
observability:
  otel:
    enabled: false
  langfuse:
    enabled: false
```

---

## 追踪数据

### 对话层面 (Conversation Trace)

每个对话创建一个根 trace，包含以下属性：

- `session.id`: 会话 ID
- `user.id`: 用户 ID
- `message.length`: 消息长度
- `input`: 用户输入消息
- `output`: 机器人回复
- `success`: 是否成功
- `error`: 错误信息（如果失败）

### 编排器层面 (Orchestrator Span)

记录编排器的执行过程：

- `orchestrator.name`: 编排器名称（react、plan-execute）
- `iterations`: 迭代次数
- `success`: 是否成功

### LLM 调用 (LLM Generation)

记录每次 LLM 调用的详细信息：

- `llm.model`: 模型名称（如 gpt-4）
- `llm.prompt.length`: Prompt 长度
- `llm.completion.length`: 完成文本长度
- `llm.usage.promptTokens`: Prompt tokens
- `llm.usage.completionTokens`: Completion tokens
- `llm.usage.totalTokens`: 总 tokens

**Langfuse 特有字段：**
- `input`: 完整的 prompt 文本
- `output`: 完整的 completion 文本
- `usage`: Token 使用详情

### 工具调用 (Tool Span)

记录工具调用的信息：

- `tool.name`: 工具名称
- `tool.success`: 是否成功
- `tool.input.length`: 输入长度
- `tool.output.length`: 输出长度

**Langfuse 特有字段：**
- `input`: 完整的工具输入
- `output`: 完整的工具输出

---

## 可视化观测

### Langfuse 界面

访问 [Langfuse Dashboard](https://cloud.langfuse.com)，可以看到：

1. **Traces 列表**
   - 所有对话的列表
   - 每个对话的执行时间、成功状态
   - Token 使用统计

2. **Trace 详情**
   - 完整的执行流程树
   - 每个 LLM 调用的 prompt 和 completion
   - 每个工具调用的输入输出

3. **Generations 视图**
   - 所有 LLM 调用的详细记录
   - Token 使用分析
   - 成本估算

4. **Analytics**
   - Token 使用趋势
   - 调用频率统计
   - 错误率分析

### Jaeger 界面

访问 http://localhost:16686，可以看到：

1. **Trace 搜索**
   - 按服务名、操作名搜索
   - 按时间范围过滤
   - 按 tag 过滤

2. **Trace 详情**
   - 完整的 span 树
   - 每个 span 的执行时间
   - 父子关系可视化

3. **依赖图**
   - 服务调用关系
   - 调用频率统计

---

## 故障排查

### 1. Langfuse 数据未显示

**检查配置：**
```bash
# 确认环境变量已设置
echo $LANGFUSE_PUBLIC_KEY
echo $LANGFUSE_SECRET_KEY
```

**查看日志：**
```
# 应该看到类似日志
Initializing Langfuse with host: https://cloud.langfuse.com
Langfuse initialized successfully
```

**常见问题：**
- API 密钥错误：检查 public-key 和 secret-key
- 网络问题：确认能访问 Langfuse 服务
- 配置未生效：确认 `enabled: true`

### 2. OpenTelemetry 数据未显示

**检查 OTLP Endpoint：**
```bash
# 测试 endpoint 是否可访问
curl http://localhost:4317
```

**查看日志：**
```
# 应该看到类似日志
Initializing OpenTelemetry with service name: agent-pattern-test and endpoint: http://localhost:4317
OpenTelemetry initialized successfully
```

**常见问题：**
- Endpoint 不可达：确认 Jaeger/Tempo 正在运行
- 端口冲突：检查 4317 端口是否被占用
- 数据未导出：检查 BatchSpanProcessor 配置

### 3. 追踪数据不完整

**可能原因：**
- ConversationTracer 未注入：检查 Spring 依赖注入
- Session ID 不一致：确保整个流程使用同一个 session ID
- 异常导致 span 未关闭：检查异常处理逻辑

**调试方法：**
```yaml
logging:
  level:
    com.example.agentpattern.observability: DEBUG
    io.opentelemetry: DEBUG
    de.langfuse: DEBUG
```

### 4. 性能问题

**优化建议：**

1. **批量导出：** OpenTelemetry 默认使用 BatchSpanProcessor，可调整批量大小
2. **采样率：** 对于高流量场景，可配置采样率
3. **异步刷新：** Langfuse 默认异步发送数据

---

## 示例追踪数据

### 完整对话示例

**请求：**
```json
{
  "message": "iPhone 14有哪些型号？价格多少？",
  "userId": "user123"
}
```

**追踪结构（Langfuse）：**

```
Trace: conversation
├─ sessionId: "session-abc123"
├─ userId: "user123"
├─ input: "iPhone 14有哪些型号？价格多少？"
├─ output: "iPhone 14系列包括..."
└─ spans:
   └─ orchestrator.react
      ├─ iterations: 2
      └─ children:
         ├─ Generation: llm.generation (Iteration 1)
         │  ├─ model: "gpt-4"
         │  ├─ input: "System: You are a helpful assistant...\nUser: iPhone 14有哪些型号？"
         │  ├─ output: "Thought: 需要搜索产品信息\nAction: product-search\nAction Input: iPhone 14"
         │  └─ usage: {promptTokens: 245, completionTokens: 32, totalTokens: 277}
         │
         ├─ Span: tool.product-search
         │  ├─ input: "iPhone 14"
         │  └─ output: "找到3个产品: iPhone 14, iPhone 14 Plus, iPhone 14 Pro..."
         │
         └─ Generation: llm.generation (Iteration 2)
            ├─ model: "gpt-4"
            ├─ input: "System: ...\n观察: 找到3个产品..."
            ├─ output: "Final Answer: iPhone 14系列包括..."
            └─ usage: {promptTokens: 312, completionTokens: 156, totalTokens: 468}
```

**总 Token 使用：** 745 tokens

---

## 最佳实践

### 1. 会话管理

- 使用稳定的 session ID 贯穿整个对话
- 在 trace metadata 中记录业务相关信息

### 2. 错误追踪

- 所有异常都会记录到 span 中
- 使用 `span.recordException()` 记录详细错误

### 3. 成本控制

- 监控 Langfuse 中的 token 使用
- 设置告警阈值
- 优化 prompt 长度

### 4. 性能监控

- 关注各个 span 的执行时间
- 识别性能瓶颈
- 优化慢查询

---

## 相关资源

- [Langfuse 文档](https://langfuse.com/docs)
- [OpenTelemetry Java SDK](https://opentelemetry.io/docs/instrumentation/java/)
- [Jaeger 文档](https://www.jaegertracing.io/docs/)
- [Grafana Tempo 文档](https://grafana.com/docs/tempo/)

---

## 技术支持

如有问题，请查看：

1. 日志文件：检查应用日志中的追踪相关日志
2. Langfuse Dashboard：查看数据是否成功上传
3. Jaeger UI：查看 OpenTelemetry 数据
4. GitHub Issues：报告 bug 或请求新功能
