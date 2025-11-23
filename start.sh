#!/bin/bash

# 加载环境变量
set -a
source .env
set +a

# 验证配置
echo "==================================================="
echo "已加载环境变量:"
echo "AZURE_OPENAI_ENDPOINT: $AZURE_OPENAI_ENDPOINT"
echo "AZURE_OPENAI_DEPLOYMENT_NAME: $AZURE_OPENAI_DEPLOYMENT_NAME"
echo "AZURE_OPENAI_API_KEY: ${AZURE_OPENAI_API_KEY:0:10}...${AZURE_OPENAI_API_KEY: -4}"
echo "==================================================="
echo ""
echo "启动应用..."
echo ""

# 启动 Spring Boot 应用
mvn spring-boot:run
