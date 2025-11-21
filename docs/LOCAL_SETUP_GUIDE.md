# 本地测试运行完整指南

本文档提供了在本地环境中测试和运行 Agent Pattern Test Framework 的详细说明。

## 目录
1. [系统要求](#系统要求)
2. [安装步骤](#安装步骤)
3. [数据准备](#数据准备)
4. [运行方案](#运行方案)
5. [测试方案](#测试方案)
6. [故障排除](#故障排除)

---

## 系统要求

### 必需
- **Python**: 3.8 或更高版本
- **操作系统**: Linux, macOS, 或 Windows (WSL)
- **内存**: 至少 2GB RAM
- **磁盘空间**: 至少 500MB

### 可选
- **API Keys**: OpenAI 或 Anthropic API key（用于真实测试）
- **Git**: 用于版本控制

---

## 安装步骤

### 方法 1: 自动安装（推荐）

```bash
# 1. 进入项目目录
cd agent-pattern-test

# 2. 给安装脚本执行权限
chmod +x scripts/setup.sh

# 3. 运行安装脚本
./scripts/setup.sh
```

安装脚本会自动完成以下操作：
- ✓ 检查 Python 版本
- ✓ 创建虚拟环境
- ✓ 安装所有依赖
- ✓ 生成 .env 配置文件
- ✓ 生成测试数据
- ✓ 创建必要的目录

### 方法 2: 手动安装

```bash
# 1. 创建虚拟环境
python3 -m venv venv

# 2. 激活虚拟环境
source venv/bin/activate  # Linux/macOS
# 或
venv\Scripts\activate  # Windows

# 3. 升级 pip
pip install --upgrade pip

# 4. 安装依赖
pip install -r requirements.txt

# 5. 创建 .env 文件
cp .env.example .env

# 6. 生成测试数据
python scripts/generate_test_data.py

# 7. 创建日志目录
mkdir -p logs
```

---

## 数据准备

### 1. 自动生成测试数据

运行数据生成脚本：

```bash
python scripts/generate_test_data.py
```

这将生成以下测试数据：

| 文件名 | 描述 | 测试数量 |
|--------|------|----------|
| `all_test_cases.json` | 所有测试用例 | 10+ |
| `mathematical_test_cases.json` | 数学计算测试 | 2 |
| `information_retrieval_test_cases.json` | 信息检索测试 | 2 |
| `multi_step_test_cases.json` | 多步骤任务测试 | 2 |
| `reasoning_test_cases.json` | 推理测试 | 2 |
| `planning_test_cases.json` | 规划测试 | 2 |
| `test_summary.json` | 测试数据摘要 | - |

### 2. 验证数据生成

```bash
# 检查文件是否生成
ls -la data/test_cases/

# 查看测试数据摘要
cat data/test_cases/test_summary.json
```

### 3. 自定义测试数据

创建自定义测试文件 `data/test_cases/custom_tests.json`:

```json
[
  {
    "id": "custom_001",
    "query": "你的测试问题",
    "expected_tools": ["calculator"],
    "difficulty": "easy",
    "category": "custom"
  }
]
```

---

## 运行方案

### 方案 A: Mock 模式（无需 API Key）

**适用场景**: 测试框架功能、开发调试

```bash
# 1. 运行 Mock 演示
python scripts/run_demo.py

# 2. 运行 Mock 测试
python scripts/run_tests.py --mock

# 3. 交互式 Mock 模式
python scripts/interactive.py
```

**特点**:
- ✓ 无需 API Key
- ✓ 快速测试框架功能
- ✓ 无 API 调用成本
- ✗ 响应为模拟数据

### 方案 B: 真实 API 模式

**适用场景**: 生产测试、性能评估

#### 步骤 1: 配置 API Key

编辑 `.env` 文件：

```bash
# 使用 OpenAI
OPENAI_API_KEY=sk-your-openai-key-here
DEFAULT_MODEL=gpt-4

# 或使用 Anthropic
ANTHROPIC_API_KEY=your-anthropic-key-here
DEFAULT_MODEL=claude-3-sonnet-20240229

# 其他配置
TEMPERATURE=0.7
MAX_TOKENS=2000
```

#### 步骤 2: 运行真实测试

```bash
# 1. 运行真实 API 演示
python scripts/run_demo.py --real

# 2. 运行完整测试套件
python scripts/run_tests.py

# 3. 交互式真实模式
python scripts/interactive.py
```

### 方案 C: 交互式测试

启动交互式 CLI：

```bash
python scripts/interactive.py
```

交互式界面提供：
1. 选择不同的 Agent 模式
2. 实时输入查询
3. 查看详细的推理过程
4. 查看执行的操作

---

## 测试方案

### 1. 单元测试

运行单元测试：

```bash
# 运行所有测试
pytest tests/ -v

# 运行特定测试文件
pytest tests/test_agents.py -v

# 运行特定测试
pytest tests/test_agents.py::TestCalculatorTool::test_basic_calculation -v
```

### 2. 集成测试

运行完整的集成测试：

```bash
# Mock 模式
python scripts/run_tests.py --mock

# 真实 API 模式
python scripts/run_tests.py
```

测试结果保存在 `data/results/` 目录。

### 3. 性能测试

比较不同 Agent 模式的性能：

```bash
# 运行性能测试
python scripts/run_tests.py

# 查看结果
cat data/results/test_results_*.json
```

性能指标包括：
- 成功率
- 平均响应时间
- 工具使用次数
- 错误率

### 4. 手动测试

使用交互式模式进行手动测试：

```bash
python scripts/interactive.py
```

测试用例示例：

```
# 数学计算
What is 25 * 4 + 10?
Calculate (100 - 25) / 5 and multiply by 3

# 信息检索
What is Python programming?
Explain AI agents

# 多步骤任务
Get weather in Beijing, if above 20°C convert to Fahrenheit
Search for machine learning, calculate years since 1959

# 推理任务
If I have $100 and spend 30%, how much left?
A train travels 80 km/h for 2.5 hours, how far?
```

---

## 运行模式对比

| 特性 | Mock 模式 | 真实 API 模式 |
|------|-----------|---------------|
| API Key | 不需要 | 需要 |
| 响应速度 | 极快 | 取决于 API |
| 成本 | 免费 | API 收费 |
| 准确性 | 模拟数据 | 真实 LLM |
| 适用场景 | 开发/测试 | 生产/评估 |

---

## 配置选项

### 环境变量 (.env)

```bash
# LLM 配置
OPENAI_API_KEY=your-key
ANTHROPIC_API_KEY=your-key
DEFAULT_MODEL=gpt-4
TEMPERATURE=0.7
MAX_TOKENS=2000

# 测试配置
TEST_DATA_PATH=data/test_cases
LOG_LEVEL=INFO
```

### YAML 配置 (config/agent_config.yaml)

```yaml
llm:
  provider: "openai"
  model: "gpt-4"
  temperature: 0.7
  max_tokens: 2000

agents:
  react:
    max_iterations: 10
    enabled: true

  chain_of_thought:
    enabled: true

  plan_execute:
    max_plan_steps: 10
    enabled: true

tools:
  calculator:
    enabled: true
  search:
    enabled: true
    mock_mode: true
  weather:
    enabled: true
    mock_mode: true
```

---

## 故障排除

### 问题 1: ModuleNotFoundError

**症状**: `ModuleNotFoundError: No module named 'xxx'`

**解决方案**:
```bash
# 确保虚拟环境已激活
source venv/bin/activate

# 重新安装依赖
pip install -r requirements.txt
```

### 问题 2: API Key 错误

**症状**: `API key not found` 或 `Authentication failed`

**解决方案**:
```bash
# 检查 .env 文件
cat .env

# 确保 API Key 正确设置
nano .env

# 验证环境变量
python -c "import os; from dotenv import load_dotenv; load_dotenv(); print(os.getenv('OPENAI_API_KEY'))"
```

### 问题 3: 找不到测试数据

**症状**: `FileNotFoundError: test_cases not found`

**解决方案**:
```bash
# 重新生成测试数据
python scripts/generate_test_data.py

# 验证文件生成
ls -la data/test_cases/
```

### 问题 4: 导入错误

**症状**: `ImportError` 或模块导入失败

**解决方案**:
```bash
# 确保在项目根目录
pwd

# 设置 PYTHONPATH
export PYTHONPATH="${PYTHONPATH}:$(pwd)"

# 或者使用完整路径运行
python -m scripts.run_demo
```

### 问题 5: 权限错误

**症状**: `Permission denied` 执行脚本

**解决方案**:
```bash
# 给脚本执行权限
chmod +x scripts/*.sh
chmod +x scripts/*.py

# 或直接用 python 运行
python scripts/run_demo.py
```

---

## 最佳实践

### 1. 开发流程

```bash
# 1. 激活虚拟环境
source venv/bin/activate

# 2. 运行 Mock 测试验证功能
python scripts/run_demo.py

# 3. 修改代码

# 4. 运行单元测试
pytest tests/ -v

# 5. 运行集成测试
python scripts/run_tests.py --mock

# 6. （可选）使用真实 API 测试
python scripts/run_demo.py --real
```

### 2. 测试流程

```bash
# 1. 生成测试数据
python scripts/generate_test_data.py

# 2. 运行快速测试（Mock）
python scripts/run_tests.py --mock

# 3. 查看结果
cat data/results/test_results_*.json

# 4. 如需真实测试，配置 API Key 后运行
python scripts/run_tests.py
```

### 3. 调试流程

```bash
# 1. 使用交互式模式
python scripts/interactive.py

# 2. 选择要测试的 Agent 模式

# 3. 输入测试查询

# 4. 查看详细的推理过程和错误信息

# 5. 修改代码后重新测试
```

---

## 性能优化建议

1. **使用更快的模型**: GPT-3.5-turbo 比 GPT-4 快且便宜
2. **调整 max_iterations**: 减少 ReAct 的最大迭代次数
3. **启用缓存**: 对相同查询缓存结果
4. **并行测试**: 使用多线程运行测试
5. **限制 token**: 减少 max_tokens 以提高速度

---

## 下一步

- 查看 [README.md](../README.md) 了解项目概述
- 查看 [QUICKSTART.md](../QUICKSTART.md) 快速入门
- 阅读源代码了解实现细节
- 添加自定义 Agent 模式
- 创建新的测试用例

---

## 获取帮助

如果遇到问题：
1. 查看本文档的故障排除部分
2. 检查 `logs/` 目录中的日志文件
3. 提交 Issue 到项目仓库
4. 查看代码中的注释和文档字符串

---

**文档版本**: 1.0
**最后更新**: 2025-11-21
