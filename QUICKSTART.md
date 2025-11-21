# 快速开始指南

这是一个快速开始指南，帮助你在 5 分钟内运行 Agent Pattern Test Framework。

## 第一步：安装（2 分钟）

```bash
# 1. 进入项目目录
cd agent-pattern-test

# 2. 运行安装脚本
chmod +x scripts/setup.sh
./scripts/setup.sh
```

安装脚本会自动：
- ✓ 创建虚拟环境
- ✓ 安装所有依赖
- ✓ 生成测试数据
- ✓ 创建 .env 配置文件

## 第二步：测试框架（1 分钟）

**无需 API Key，立即测试框架功能：**

```bash
# 激活虚拟环境（如果还没激活）
source venv/bin/activate

# 运行 Mock 模式演示
python scripts/run_demo.py
```

这会运行几个示例，展示不同的 Agent 模式如何工作。

## 第三步：配置 API（可选，2 分钟）

如果你想使用真实的 LLM：

```bash
# 编辑 .env 文件
nano .env  # 或使用你喜欢的编辑器

# 添加以下任一 API Key：
OPENAI_API_KEY=sk-your-key-here
# 或
ANTHROPIC_API_KEY=your-key-here
```

然后运行真实 API 测试：

```bash
python scripts/run_demo.py --real
```

## 第四步：交互式测试

启动交互式命令行界面：

```bash
python scripts/interactive.py
```

这会打开一个交互式终端，你可以：
1. 选择不同的 Agent 模式
2. 输入你的问题
3. 实时查看结果和推理过程

## 常用命令

```bash
# 1. Mock 模式演示（无需 API）
python scripts/run_demo.py

# 2. 真实 API 演示
python scripts/run_demo.py --real

# 3. 运行完整测试套件（Mock）
python scripts/run_tests.py --mock

# 4. 运行真实测试
python scripts/run_tests.py

# 5. 交互式模式
python scripts/interactive.py

# 6. 生成更多测试数据
python scripts/generate_test_data.py

# 7. 运行单元测试
pytest tests/

# 8. 查看帮助
python scripts/run_demo.py --help
```

## 测试问题示例

试试这些问题来测试不同的 Agent 模式：

### 数学计算
```
What is 25 * 4 + 10?
计算 (100 - 25) / 5 然后乘以 3
```

### 信息检索
```
What is Python programming language?
解释什么是 AI agents
```

### 多步骤任务
```
查询北京的天气，如果温度高于 20 度，计算华氏度
搜索机器学习的信息，然后告诉我如果它是 1959 年创建的，距今多少年
```

### 推理问题
```
If I have $100 and spend 30%, how much is left?
一辆火车以 80 km/h 行驶 2.5 小时，它能走多远？
```

## 项目结构速览

```
agent-pattern-test/
├── src/                    # 核心代码
│   ├── agents/            # Agent 基类和 LLM 客户端
│   ├── patterns/          # ReAct, CoT, Plan&Execute
│   └── tools/             # Calculator, Search, Weather
├── scripts/               # 运行脚本
│   ├── run_demo.py       # 演示
│   ├── run_tests.py      # 测试
│   └── interactive.py    # 交互式 CLI
├── data/                  # 测试数据和结果
├── config/                # 配置文件
└── tests/                 # 单元测试
```

## 下一步

1. **阅读完整文档**: 查看 `README.md` 了解详细信息
2. **自定义 Agent**: 在 `src/patterns/` 添加你自己的 Agent 模式
3. **添加工具**: 在 `src/tools/` 创建新工具
4. **运行测试**: 使用 `pytest` 运行单元测试

## 问题排查

### 问题：Import 错误

**解决**：
```bash
# 确保虚拟环境已激活
source venv/bin/activate

# 重新安装依赖
pip install -r requirements.txt
```

### 问题：API Key 错误

**解决**：
```bash
# 检查 .env 文件是否存在
ls -la .env

# 如果不存在，从模板复制
cp .env.example .env

# 编辑并添加你的 API Key
nano .env
```

### 问题：找不到测试数据

**解决**：
```bash
# 重新生成测试数据
python scripts/generate_test_data.py

# 检查数据是否生成
ls -la data/test_cases/
```

## 获取帮助

- 查看 `README.md` 了解详细文档
- 查看 `tests/test_agents.py` 了解使用示例
- 检查 `config/agent_config.yaml` 了解配置选项

## 快速检查清单

- [ ] 安装完成（运行 `./scripts/setup.sh`）
- [ ] Mock 模式可以运行（`python scripts/run_demo.py`）
- [ ] 测试数据已生成（检查 `data/test_cases/`）
- [ ] （可选）API Key 已配置（`.env` 文件）
- [ ] （可选）真实 API 可以运行（`python scripts/run_demo.py --real`）

完成这些步骤后，你就可以开始探索和测试不同的 Agent 模式了！
