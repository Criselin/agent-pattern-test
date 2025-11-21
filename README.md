# Agent Pattern Test Framework

一个用于测试和比较不同 AI Agent 设计模式的完整框架。

## 功能特点

- 🤖 **多种 Agent 模式**：支持 ReAct、Chain of Thought、Plan & Execute 等模式
- 🔧 **可扩展工具系统**：内置计算器、搜索、天气等工具，易于扩展
- 📊 **自动化测试**：完整的测试套件和性能对比
- 🎯 **Mock 模式**：无需 API 即可测试框架功能
- 📝 **详细日志**：记录 Agent 的推理过程和执行步骤

## 项目结构

```
agent-pattern-test/
├── src/
│   ├── agents/          # Agent 基础类和 LLM 客户端
│   ├── patterns/        # 不同的 Agent 模式实现
│   ├── tools/           # Agent 可用的工具
│   └── utils/           # 工具函数
├── tests/               # 单元测试
├── data/
│   ├── test_cases/      # 测试用例
│   ├── generated/       # 生成的数据
│   └── results/         # 测试结果
├── config/              # 配置文件
├── scripts/             # 运行脚本
│   ├── setup.sh         # 安装脚本
│   ├── run_demo.py      # 演示脚本
│   ├── run_tests.py     # 测试脚本
│   └── generate_test_data.py  # 数据生成脚本
└── logs/                # 日志文件
```

## 快速开始

### 1. 环境准备

```bash
# 克隆项目（如果还没有）
cd agent-pattern-test

# 运行安装脚本
chmod +x scripts/setup.sh
./scripts/setup.sh
```

### 2. 配置 API Keys

编辑 `.env` 文件，添加你的 API Key：

```bash
# 使用 OpenAI
OPENAI_API_KEY=sk-your-key-here

# 或使用 Anthropic
ANTHROPIC_API_KEY=your-key-here
```

### 3. 运行演示

```bash
# Mock 模式（无需 API Key）
python scripts/run_demo.py

# 真实 API 调用模式
python scripts/run_demo.py --real
```

### 4. 运行完整测试

```bash
# Mock 模式测试
python scripts/run_tests.py --mock

# 真实 API 测试
python scripts/run_tests.py
```

## 支持的 Agent 模式

### 1. Chain of Thought (CoT)
逐步推理模式，将复杂问题分解为多个步骤。

**适用场景**：
- 数学推理
- 逻辑分析
- 需要展示推理过程的任务

### 2. ReAct (Reasoning and Acting)
推理和行动交替进行的模式。

**适用场景**：
- 需要使用工具的任务
- 多步骤问题解决
- 需要根据观察调整策略

### 3. Plan and Execute
先制定计划，再逐步执行的模式。

**适用场景**：
- 复杂的多步骤任务
- 项目规划
- 需要结构化执行的任务

## 数据准备方案

### 自动生成测试数据

```bash
# 生成测试用例
python scripts/generate_test_data.py
```

这将在 `data/test_cases/` 目录下生成以下文件：
- `all_test_cases.json` - 所有测试用例
- `mathematical_test_cases.json` - 数学类测试
- `information_retrieval_test_cases.json` - 信息检索测试
- `multi_step_test_cases.json` - 多步骤测试
- `reasoning_test_cases.json` - 推理测试
- `planning_test_cases.json` - 规划测试
- `test_summary.json` - 测试数据摘要

### 自定义测试数据

在 `data/test_cases/` 目录下创建 JSON 文件：

```json
[
  {
    "id": "custom_001",
    "query": "你的测试问题",
    "expected_tools": ["calculator", "search"],
    "difficulty": "medium",
    "category": "custom"
  }
]
```

## 使用示例

### Python 代码示例

```python
from src.agents.llm_client import LLMClient
from src.patterns.react_agent import ReActAgent
from src.tools.calculator import CalculatorTool

# 创建 LLM 客户端
llm_client = LLMClient(provider="openai", model="gpt-4")

# 创建工具
tools = [CalculatorTool()]

# 创建 Agent
agent = ReActAgent("MyAgent", llm_client, tools=tools)

# 处理查询
response = agent.process("计算 25 * 4 + 10 是多少？")

print(f"答案: {response.content}")
print(f"推理过程: {response.reasoning}")
print(f"执行的操作: {response.actions_taken}")
```

## 配置选项

编辑 `config/agent_config.yaml` 来自定义配置：

```yaml
llm:
  provider: "openai"  # 或 "anthropic"
  model: "gpt-4"
  temperature: 0.7
  max_tokens: 2000

agents:
  react:
    max_iterations: 10

tools:
  calculator:
    enabled: true
  search:
    enabled: true
    mock_mode: true
```

## 扩展框架

### 添加新的 Agent 模式

1. 在 `src/patterns/` 创建新文件
2. 继承 `BaseAgent` 类
3. 实现 `process()` 方法

```python
from src.agents.base_agent import BaseAgent, AgentResponse

class MyCustomAgent(BaseAgent):
    def process(self, user_input: str, **kwargs) -> AgentResponse:
        # 你的实现
        pass
```

### 添加新工具

1. 在 `src/tools/` 创建新文件
2. 继承 `BaseTool` 类
3. 实现 `execute()` 方法

```python
from src.tools.base_tool import BaseTool

class MyTool(BaseTool):
    def __init__(self):
        super().__init__(
            name="mytool",
            description="工具描述"
        )

    def execute(self, input_data: str) -> str:
        # 你的实现
        return result
```

## 测试结果

测试结果会保存在 `data/results/` 目录，包含：
- 每个 Agent 的成功率
- 平均响应时间
- 工具使用统计
- 详细的测试日志

## 常见问题

### 1. 没有 API Key 能测试吗？

可以！使用 `--mock` 参数：
```bash
python scripts/run_demo.py
python scripts/run_tests.py --mock
```

### 2. 如何切换不同的 LLM 提供商？

修改 `.env` 文件或 `config/agent_config.yaml`：
```yaml
llm:
  provider: "anthropic"  # 改为 anthropic
  model: "claude-3-sonnet-20240229"
```

### 3. 如何添加更多测试用例？

编辑 `scripts/generate_test_data.py` 或直接在 `data/test_cases/` 添加 JSON 文件。

## 性能优化建议

1. **使用更快的模型**：对于简单任务使用 GPT-3.5 或 Claude Haiku
2. **调整 max_iterations**：根据任务复杂度调整 ReAct 的最大迭代次数
3. **缓存结果**：对重复查询启用缓存
4. **并行测试**：使用多线程运行测试

## 依赖项

主要依赖：
- `openai>=1.0.0` - OpenAI API
- `anthropic>=0.18.0` - Anthropic API
- `pydantic>=2.0.0` - 数据验证
- `rich>=13.0.0` - 终端输出美化
- `pytest>=7.4.0` - 测试框架

## 贡献指南

欢迎贡献！请：
1. Fork 本仓库
2. 创建特性分支
3. 提交变更
4. 发起 Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue。

---

**快速命令参考**：

```bash
# 安装
./scripts/setup.sh

# Mock 演示
python scripts/run_demo.py

# 真实 API 演示
python scripts/run_demo.py --real

# 运行测试
python scripts/run_tests.py --mock

# 生成数据
python scripts/generate_test_data.py
```
