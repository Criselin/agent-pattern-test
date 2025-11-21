# 本地测试运行方案与数据准备指南

## 目录
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [详细安装步骤](#详细安装步骤)
- [数据准备方案](#数据准备方案)
- [运行方案](#运行方案)
- [测试验证](#测试验证)
- [常见问题](#常见问题)

---

## 环境要求

### 必需软件
- **JDK**: 17 或以上版本
- **Maven**: 3.8+ 或 Gradle 7.5+
- **Docker**: 20.10+ (用于本地数据库和中间件)
- **Git**: 2.30+

### 推荐配置
- **内存**: 最少 8GB RAM
- **磁盘空间**: 最少 10GB 可用空间
- **操作系统**: Linux / macOS / Windows 10+

---

## 快速开始

```bash
# 1. 克隆并切换分支
git clone <repository-url>
cd agent-pattern-test
git checkout claude/spring-ai-agent-chatbot-01NSeLY8i4z9nC6dcyKw2nPc

# 2. 启动依赖服务
docker-compose up -d

# 3. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入必要的配置

# 4. 启动应用
./mvnw spring-boot:run
# 或者
./gradlew bootRun

# 5. 访问应用
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## 详细安装步骤

### 1. JDK 安装

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

#### macOS
```bash
brew install openjdk@17
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version
```

#### Windows
1. 下载 OpenJDK 17: https://adoptium.net/
2. 安装并配置 JAVA_HOME 环境变量
3. 验证: `java -version`

### 2. Docker 安装

#### Linux
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
# 重新登录以应用组权限
docker --version
```

#### macOS / Windows
下载并安装 Docker Desktop: https://www.docker.com/products/docker-desktop/

### 3. Maven/Gradle 安装 (可选)

项目通常包含 wrapper 脚本 (mvnw/gradlew)，无需单独安装。

---

## 数据准备方案

### 方案一: Docker Compose (推荐)

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  # PostgreSQL 数据库
  postgres:
    image: postgres:15-alpine
    container_name: chatbot-postgres
    environment:
      POSTGRES_DB: chatbot_db
      POSTGRES_USER: chatbot_user
      POSTGRES_PASSWORD: chatbot_pass_2024
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/init-db:/docker-entrypoint-initdb.d
    networks:
      - chatbot-network

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: chatbot-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - chatbot-network
    command: redis-server --appendonly yes

  # Vector Database (Qdrant)
  qdrant:
    image: qdrant/qdrant:latest
    container_name: chatbot-qdrant
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage
    networks:
      - chatbot-network

  # Elasticsearch (可选 - 用于全文搜索)
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: chatbot-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es_data:/usr/share/elasticsearch/data
    networks:
      - chatbot-network

  # RabbitMQ (可选 - 用于消息队列)
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: chatbot-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin_pass_2024
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - chatbot-network

volumes:
  postgres_data:
  redis_data:
  qdrant_data:
  es_data:
  rabbitmq_data:

networks:
  chatbot-network:
    driver: bridge
```

**启动所有服务:**
```bash
docker-compose up -d
```

**查看服务状态:**
```bash
docker-compose ps
```

**查看日志:**
```bash
docker-compose logs -f [service-name]
```

### 方案二: 手动安装本地服务

#### PostgreSQL
```bash
# Linux
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql

# macOS
brew install postgresql@15
brew services start postgresql@15

# 创建数据库和用户
sudo -u postgres psql
CREATE DATABASE chatbot_db;
CREATE USER chatbot_user WITH ENCRYPTED PASSWORD 'chatbot_pass_2024';
GRANT ALL PRIVILEGES ON DATABASE chatbot_db TO chatbot_user;
\q
```

#### Redis
```bash
# Linux
sudo apt install redis-server
sudo systemctl start redis

# macOS
brew install redis
brew services start redis
```

### 数据库初始化脚本

创建 `docker/init-db/01_schema.sql`:

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    session_id VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 消息历史表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_sessions(id),
    role VARCHAR(20) NOT NULL, -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    metadata JSONB,
    token_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent 配置表
CREATE TABLE IF NOT EXISTS agent_configs (
    id BIGSERIAL PRIMARY KEY,
    agent_name VARCHAR(100) UNIQUE NOT NULL,
    agent_type VARCHAR(50) NOT NULL, -- 'conversational', 'task', 'analytical'
    model_name VARCHAR(100) NOT NULL,
    system_prompt TEXT,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INTEGER DEFAULT 2000,
    tools JSONB, -- 可用工具列表
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档表
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    document_type VARCHAR(50), -- 'pdf', 'text', 'web', 'api'
    source_url VARCHAR(500),
    metadata JSONB,
    embedding_id VARCHAR(100), -- 向量数据库中的ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_chat_messages_created ON chat_messages(created_at);
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_knowledge_docs_type ON knowledge_documents(document_type);
```

创建 `docker/init-db/02_seed_data.sql`:

```sql
-- 插入测试用户
INSERT INTO users (username, email, password_hash) VALUES
('admin', 'admin@chatbot.local', '$2a$10$xHzqF8qSXKE8K9yMEz.j5.VNyY4kYqKvVzJLKvZLvXQvZqXQvZqXQ'),
('test_user', 'test@chatbot.local', '$2a$10$xHzqF8qSXKE8K9yMEz.j5.VNyY4kYqKvVzJLKvZLvXQvZqXQvZqXQ'),
('demo_user', 'demo@chatbot.local', '$2a$10$xHzqF8qSXKE8K9yMEz.j5.VNyY4kYqKvVzJLKvZLvXQvZqXQvZqXQ');

-- 插入 Agent 配置
INSERT INTO agent_configs (agent_name, agent_type, model_name, system_prompt, temperature, max_tokens, tools) VALUES
(
    'general_assistant',
    'conversational',
    'gpt-4',
    'You are a helpful AI assistant. Answer questions concisely and accurately.',
    0.7,
    2000,
    '["web_search", "calculator", "code_interpreter"]'::jsonb
),
(
    'technical_expert',
    'task',
    'gpt-4',
    'You are a technical expert specializing in software development. Provide detailed technical explanations.',
    0.5,
    3000,
    '["code_interpreter", "database_query", "api_call"]'::jsonb
),
(
    'data_analyst',
    'analytical',
    'gpt-4',
    'You are a data analyst. Help users analyze data and generate insights.',
    0.3,
    2500,
    '["data_analysis", "visualization", "sql_query"]'::jsonb
);

-- 插入示例知识库文档
INSERT INTO knowledge_documents (title, content, document_type, metadata) VALUES
(
    'Spring AI 简介',
    'Spring AI 是一个为 AI 应用开发提供的 Spring 框架扩展。它简化了与各种 AI 服务的集成，包括 OpenAI、Azure OpenAI、Hugging Face 等。主要特性包括：1. 统一的 API 抽象 2. 向量存储集成 3. 提示词模板管理 4. 函数调用支持',
    'text',
    '{"category": "documentation", "language": "zh-CN"}'::jsonb
),
(
    'Agent 设计模式',
    'Agent 设计模式是一种用于构建智能系统的架构模式。核心概念包括：1. Perception (感知) 2. Decision Making (决策) 3. Action (执行) 4. Learning (学习)。常见的实现模式有 ReAct、Plan-and-Execute、Reflection 等。',
    'text',
    '{"category": "design_pattern", "language": "zh-CN"}'::jsonb
);

-- 插入示例对话会话
INSERT INTO chat_sessions (user_id, session_id, title) VALUES
(1, 'session_001', '测试对话 1'),
(2, 'session_002', 'Spring AI 咨询');

-- 插入示例消息
INSERT INTO chat_messages (session_id, role, content, token_count) VALUES
(1, 'user', '你好，请介绍一下 Spring AI', 15),
(1, 'assistant', 'Spring AI 是 Spring 生态系统中的一个项目，旨在简化 AI 应用的开发...', 120),
(2, 'user', '如何实现一个简单的 chatbot？', 12),
(2, 'assistant', '实现一个简单的 chatbot 需要以下步骤：1. 选择合适的 AI 模型...', 200);
```

---

## 运行方案

### 配置文件

创建 `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: spring-ai-agent-chatbot

  datasource:
    url: jdbc:postgresql://localhost:5432/chatbot_db
    username: chatbot_user
    password: chatbot_pass_2024
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 6000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
          max-tokens: 2000

    vectorstore:
      qdrant:
        host: localhost
        port: 6333
        collection-name: chatbot_embeddings

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    root: INFO
    com.example.chatbot: DEBUG
    org.springframework.ai: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

创建 `.env` 文件:

```bash
# OpenAI 配置
OPENAI_API_KEY=sk-your-openai-api-key-here
OPENAI_BASE_URL=https://api.openai.com

# 或者使用 Azure OpenAI
# AZURE_OPENAI_API_KEY=your-azure-key
# AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com

# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=chatbot_db
DB_USER=chatbot_user
DB_PASSWORD=chatbot_pass_2024

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# 应用配置
SERVER_PORT=8080
LOG_LEVEL=DEBUG

# 安全配置
JWT_SECRET=your-jwt-secret-key-here-at-least-256-bits
JWT_EXPIRATION=86400000

# 向量数据库配置
QDRANT_HOST=localhost
QDRANT_PORT=6333
```

### 启动步骤

#### 1. 环境准备
```bash
# 确保 Docker 服务已启动
docker-compose up -d

# 等待服务启动完成 (约 30 秒)
sleep 30

# 验证服务状态
docker-compose ps
```

#### 2. 构建项目

**使用 Maven:**
```bash
./mvnw clean install -DskipTests
```

**使用 Gradle:**
```bash
./gradlew clean build -x test
```

#### 3. 运行应用

**开发模式 (Maven):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**开发模式 (Gradle):**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**生产模式:**
```bash
java -jar target/spring-ai-agent-chatbot-1.0.0.jar
```

#### 4. 使用脚本启动 (推荐)

创建 `start.sh`:

```bash
#!/bin/bash

echo "=== Spring AI Agent Chatbot 启动脚本 ==="

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "❌ Java 未安装，请先安装 JDK 17+"
    exit 1
fi

echo "✅ 环境检查通过"

# 启动 Docker 服务
echo "🚀 启动依赖服务..."
docker-compose up -d

# 等待服务就绪
echo "⏳ 等待服务启动..."
sleep 30

# 检查 PostgreSQL
until docker exec chatbot-postgres pg_isready -U chatbot_user > /dev/null 2>&1; do
    echo "⏳ 等待 PostgreSQL..."
    sleep 2
done
echo "✅ PostgreSQL 已就绪"

# 检查 Redis
until docker exec chatbot-redis redis-cli ping > /dev/null 2>&1; do
    echo "⏳ 等待 Redis..."
    sleep 2
done
echo "✅ Redis 已就绪"

# 加载环境变量
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ 环境变量已加载"
else
    echo "⚠️  .env 文件不存在，使用默认配置"
fi

# 启动应用
echo "🚀 启动 Spring Boot 应用..."
if [ -f "mvnw" ]; then
    ./mvnw spring-boot:run
elif [ -f "gradlew" ]; then
    ./gradlew bootRun
else
    echo "❌ 未找到 mvnw 或 gradlew"
    exit 1
fi
```

赋予执行权限并运行:
```bash
chmod +x start.sh
./start.sh
```

创建 `stop.sh`:

```bash
#!/bin/bash

echo "=== 停止 Spring AI Agent Chatbot ==="

# 停止 Spring Boot 应用
echo "🛑 停止应用..."
pkill -f "spring-boot:run"
pkill -f "spring-ai-agent-chatbot"

# 停止 Docker 服务
echo "🛑 停止依赖服务..."
docker-compose down

echo "✅ 所有服务已停止"
```

赋予执行权限:
```bash
chmod +x stop.sh
```

---

## 测试验证

### 1. 健康检查

```bash
# 应用健康检查
curl http://localhost:8080/api/actuator/health

# 预期输出
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 2. 数据库连接测试

```bash
# 连接 PostgreSQL
docker exec -it chatbot-postgres psql -U chatbot_user -d chatbot_db

# 执行查询
SELECT * FROM users;
SELECT * FROM agent_configs;
\q
```

### 3. Redis 连接测试

```bash
# 连接 Redis
docker exec -it chatbot-redis redis-cli

# 测试命令
PING
SET test_key "Hello"
GET test_key
EXIT
```

### 4. API 测试

创建 `test_api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "=== API 测试脚本 ==="

# 1. 健康检查
echo -e "\n1. 健康检查"
curl -s $BASE_URL/actuator/health | jq '.'

# 2. 创建对话会话
echo -e "\n2. 创建对话会话"
SESSION_RESPONSE=$(curl -s -X POST $BASE_URL/chat/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "测试会话"
  }')
echo $SESSION_RESPONSE | jq '.'
SESSION_ID=$(echo $SESSION_RESPONSE | jq -r '.sessionId')

# 3. 发送消息
echo -e "\n3. 发送消息"
curl -s -X POST $BASE_URL/chat/sessions/$SESSION_ID/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "你好，请介绍一下自己",
    "agentName": "general_assistant"
  }' | jq '.'

# 4. 获取会话历史
echo -e "\n4. 获取会话历史"
curl -s $BASE_URL/chat/sessions/$SESSION_ID/messages | jq '.'

# 5. 获取 Agent 列表
echo -e "\n5. 获取 Agent 列表"
curl -s $BASE_URL/agents | jq '.'

echo -e "\n✅ 测试完成"
```

### 5. 使用 Postman/Insomnia

导入以下 Collection:

```json
{
  "info": {
    "name": "Spring AI Agent Chatbot",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/actuator/health"
      }
    },
    {
      "name": "Create Chat Session",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/chat/sessions",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"userId\": 1,\n  \"title\": \"New Conversation\"\n}"
        }
      }
    },
    {
      "name": "Send Message",
      "request": {
        "method": "POST",
        "url": "{{base_url}}/chat/sessions/{{session_id}}/messages",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"content\": \"Hello, how can you help me?\",\n  \"agentName\": \"general_assistant\"\n}"
        }
      }
    },
    {
      "name": "Get Session History",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/chat/sessions/{{session_id}}/messages"
      }
    },
    {
      "name": "List Agents",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/agents"
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080/api"
    },
    {
      "key": "session_id",
      "value": ""
    }
  ]
}
```

### 6. 集成测试

运行集成测试:

```bash
# Maven
./mvnw test

# Gradle
./gradlew test

# 运行特定测试
./mvnw test -Dtest=ChatbotIntegrationTest
```

---

## 常见问题

### Q1: 启动时提示端口被占用

**问题:** `Port 8080 already in use`

**解决方案:**
```bash
# 查找占用端口的进程
lsof -i :8080
# 或
netstat -ano | grep 8080

# 杀死进程
kill -9 <PID>

# 或者修改应用端口
# 在 application.yml 中修改 server.port
```

### Q2: 数据库连接失败

**问题:** `Connection refused: localhost:5432`

**解决方案:**
```bash
# 检查 Docker 容器状态
docker ps | grep postgres

# 查看容器日志
docker logs chatbot-postgres

# 重启容器
docker-compose restart postgres

# 验证数据库连接
docker exec -it chatbot-postgres psql -U chatbot_user -d chatbot_db
```

### Q3: OpenAI API 调用失败

**问题:** `API key not valid`

**解决方案:**
1. 检查 `.env` 文件中的 `OPENAI_API_KEY`
2. 确保 API key 有效且有足够余额
3. 检查网络连接和防火墙设置
4. 考虑使用代理:
   ```yaml
   spring:
     ai:
       openai:
         base-url: http://your-proxy-url
   ```

### Q4: 向量数据库连接失败

**问题:** Qdrant 无法连接

**解决方案:**
```bash
# 检查 Qdrant 容器
docker logs chatbot-qdrant

# 测试连接
curl http://localhost:6333/collections

# 重启服务
docker-compose restart qdrant
```

### Q5: 内存不足

**问题:** `OutOfMemoryError: Java heap space`

**解决方案:**
```bash
# 增加 JVM 堆内存
java -Xms512m -Xmx2048m -jar target/spring-ai-agent-chatbot-1.0.0.jar

# 或在 mvnw/gradlew 启动时
export MAVEN_OPTS="-Xms512m -Xmx2048m"
./mvnw spring-boot:run
```

### Q6: Docker Compose 启动慢

**解决方案:**
```bash
# 拉取镜像
docker-compose pull

# 使用国内镜像源
# 编辑 /etc/docker/daemon.json (Linux)
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://registry.docker-cn.com"
  ]
}

# 重启 Docker
sudo systemctl restart docker
```

### Q7: 如何重置数据库

```bash
# 停止服务
docker-compose down

# 删除数据卷
docker volume rm agent-pattern-test_postgres_data

# 重新启动
docker-compose up -d
```

### Q8: 如何查看详细日志

```bash
# 应用日志
tail -f logs/spring-ai-chatbot.log

# Docker 服务日志
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f qdrant

# 所有服务日志
docker-compose logs -f
```

---

## 进阶配置

### 生产环境配置

创建 `application-prod.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
  jpa:
    show-sql: false

logging:
  level:
    root: WARN
    com.example.chatbot: INFO
  file:
    name: /var/log/chatbot/application.log

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 性能优化

```yaml
spring:
  ai:
    openai:
      chat:
        options:
          timeout: 60s
          max-retries: 3

  cache:
    type: redis
    redis:
      time-to-live: 600000 # 10 分钟

  task:
    execution:
      pool:
        core-size: 8
        max-size: 16
        queue-capacity: 100
```

### 监控配置

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
```

---

## 联系与支持

如有问题，请参考:
- 项目文档: `README.md`
- Issue 追踪: GitHub Issues
- 邮件支持: support@example.com

---

**版本:** 1.0.0
**最后更新:** 2025-11-21
