#!/bin/bash

# Reolink 产品咨询功能测试脚本

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "======================================"
echo "  Reolink 产品咨询功能测试"
echo "======================================"

# 测试 1: 搜索 Reolink 品牌
echo -e "\n${BLUE}[测试 1] 搜索 Reolink 品牌产品${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "有哪些 Reolink 产品？"}')

if echo $response | grep -q "Reolink"; then
    echo -e "${GREEN}✓ Reolink 品牌搜索成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ Reolink 品牌搜索失败${NC}"
    echo "响应: $response"
fi

# 测试 2: 搜索摄像头类别
echo -e "\n${BLUE}[测试 2] 搜索摄像头类别${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "推荐一款户外摄像头"}')

if echo $response | grep -q "摄像头"; then
    echo -e "${GREEN}✓ 摄像头类别搜索成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 摄像头类别搜索失败${NC}"
fi

# 测试 3: 具体产品咨询
echo -e "\n${BLUE}[测试 3] 咨询 Argus 4 Pro 产品${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Argus 4 Pro 有什么特点？"}')

if echo $response | grep -q "Argus 4 Pro"; then
    echo -e "${GREEN}✓ 具体产品咨询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 具体产品咨询失败${NC}"
fi

# 测试 4: 4K 摄像头搜索
echo -e "\n${BLUE}[测试 4] 搜索 4K 摄像头${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "有哪些 4K 摄像头？"}')

if echo $response | grep -q "4K"; then
    echo -e "${GREEN}✓ 4K 摄像头搜索成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 4K 摄像头搜索失败${NC}"
fi

# 测试 5: 价格区间查询
echo -e "\n${BLUE}[测试 5] 查询 Reolink 产品价格${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Reolink 门铃多少钱？"}')

if echo $response | grep -q "¥"; then
    echo -e "${GREEN}✓ 价格查询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 价格查询失败${NC}"
fi

# 测试 6: 监控套装咨询
echo -e "\n${BLUE}[测试 6] 咨询监控套装${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "我想买一套完整的监控系统，有推荐吗？"}')

if echo $response | grep -q "套装"; then
    echo -e "${GREEN}✓ 监控套装咨询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 监控套装咨询失败${NC}"
fi

# 测试 7: 技术问题咨询（知识库）
echo -e "\n${BLUE}[测试 7] 技术问题咨询（知识库检索）${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Reolink 摄像头怎么安装？"}')

if echo $response | grep -q "安装"; then
    echo -e "${GREEN}✓ 技术问题咨询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ 技术问题咨询失败${NC}"
fi

# 测试 8: WiFi 连接问题
echo -e "\n${BLUE}[测试 8] WiFi 连接问题咨询${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Reolink 摄像头连接不上 WiFi 怎么办？"}')

if echo $response | grep -q "WiFi"; then
    echo -e "${GREEN}✓ WiFi 问题咨询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ WiFi 问题咨询失败${NC}"
fi

# 测试 9: PoE 摄像头咨询
echo -e "\n${BLUE}[测试 9] PoE 摄像头咨询${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "什么是 PoE 摄像头？有什么优势？"}')

if echo $response | grep -q "PoE"; then
    echo -e "${GREEN}✓ PoE 咨询成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${RED}✗ PoE 咨询失败${NC}"
fi

# 测试 10: 混合品牌搜索
echo -e "\n${BLUE}[测试 10] 混合品牌搜索（Apple + Reolink）${NC}"
response=$(curl -s -X POST $BASE_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "有哪些品牌的产品？"}')

if echo $response | grep -q "Apple" && echo $response | grep -q "Reolink"; then
    echo -e "${GREEN}✓ 混合品牌搜索成功${NC}"
    echo "响应摘要: $(echo $response | jq -r '.message' | head -c 200)..."
else
    echo -e "${YELLOW}⚠ 混合品牌搜索部分成功${NC}"
fi

echo -e "\n======================================"
echo "  测试完成"
echo "======================================"
echo ""
echo -e "${YELLOW}提示：${NC}"
echo "1. 这些测试需要应用正在运行（mvn spring-boot:run）"
echo "2. 需要配置有效的 OpenAI API Key"
echo "3. 详细的响应内容可以查看应用日志"
echo ""
