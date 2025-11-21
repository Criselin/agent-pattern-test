package com.example.agentpattern.tools;

import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * FAQ工具
 * 用于查询常见问题解答
 */
@Slf4j
@Component
public class FAQTool implements Tool {

    private final ToolRegistry toolRegistry;

    // 模拟FAQ数据库
    private static final Map<String, String> FAQ_DATABASE = new HashMap<>();

    static {
        FAQ_DATABASE.put("退货", """
                退货政策:
                1. 自收到商品之日起7天内可申请退货
                2. 商品需保持原包装完好,未使用
                3. 提供购买凭证和订单号
                4. 联系客服申请退货,审核通过后寄回商品
                5. 收到退货后3-5个工作日退款到原支付账户
                """);

        FAQ_DATABASE.put("换货", """
                换货政策:
                1. 商品质量问题可在7天内申请换货
                2. 非质量问题换货需在收货3天内申请
                3. 保持商品包装完好
                4. 联系客服提供订单号和换货原因
                5. 换货商品将在3-5个工作日内寄出
                """);

        FAQ_DATABASE.put("配送", """
                配送信息:
                1. 支持全国配送,偏远地区除外
                2. 订单金额满99元免运费
                3. 一般3-5个工作日送达
                4. 支持顺丰、京东物流等多种配送方式
                5. 可在订单详情页查看物流信息
                """);

        FAQ_DATABASE.put("支付", """
                支付方式:
                1. 支持微信支付、支付宝
                2. 支持信用卡、借记卡支付
                3. 支持花呗、京东白条分期
                4. 企业订单支持对公转账
                5. 所有支付均采用加密技术保障安全
                """);

        FAQ_DATABASE.put("保修", """
                保修政策:
                1. 所有商品享受国家三包政策
                2. 手机、电脑等电子产品保修1年
                3. 配件类产品保修90天
                4. 人为损坏不在保修范围内
                5. 保修期内免费维修或更换
                """);

        FAQ_DATABASE.put("发票", """
                发票说明:
                1. 支持开具电子发票和纸质发票
                2. 下单时可选择是否需要发票
                3. 电子发票将发送到注册邮箱
                4. 纸质发票随商品一起配送
                5. 发票抬头可选择个人或公司
                """);
    }

    public FAQTool(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
        log.info("FAQTool registered");
    }

    @Override
    public String getName() {
        return "faq";
    }

    @Override
    public String getDescription() {
        return "Answer frequently asked questions. Input should be a question keyword (e.g., '退货', '配送', '支付', '保修', '发票', '换货')";
    }

    @Override
    public ToolResult execute(String input) {
        try {
            String query = input.trim();
            log.debug("Querying FAQ with: {}", query);

            // 查找匹配的FAQ
            String answer = null;
            for (Map.Entry<String, String> entry : FAQ_DATABASE.entrySet()) {
                if (query.contains(entry.getKey()) || entry.getKey().contains(query)) {
                    answer = entry.getValue();
                    break;
                }
            }

            if (answer == null) {
                // 如果没有找到,返回可用的FAQ主题
                String availableTopics = String.join("、", FAQ_DATABASE.keySet());
                return ToolResult.success(
                        "未找到相关问题的答案。\n" +
                        "您可以询问以下主题的问题: " + availableTopics
                );
            }

            return ToolResult.success(answer);

        } catch (Exception e) {
            log.error("Error querying FAQ", e);
            return ToolResult.failure("查询FAQ时出错: " + e.getMessage());
        }
    }

    @Override
    public String getParameterSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "query": {
                      "type": "string",
                      "description": "问题关键词"
                    }
                  },
                  "required": ["query"]
                }
                """;
    }
}
