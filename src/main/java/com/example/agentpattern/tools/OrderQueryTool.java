package com.example.agentpattern.tools;

import com.example.agentpattern.agent.tool.Tool;
import com.example.agentpattern.agent.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单查询工具
 * 用于查询订单信息
 */
@Slf4j
@Component
public class OrderQueryTool implements Tool {

    private final ToolRegistry toolRegistry;

    // 模拟订单数据库
    private static final Map<String, OrderInfo> ORDER_DATABASE = new HashMap<>();

    static {
        ORDER_DATABASE.put("ORD001", new OrderInfo("ORD001", "iPhone 15 Pro", "已发货", "2024-01-15", "顺丰快递: SF1234567890"));
        ORDER_DATABASE.put("ORD002", new OrderInfo("ORD002", "MacBook Pro 16", "配送中", "2024-01-18", "京东物流: JD9876543210"));
        ORDER_DATABASE.put("ORD003", new OrderInfo("ORD003", "AirPods Pro", "已签收", "2024-01-10", "已于2024-01-12签收"));
        ORDER_DATABASE.put("ORD004", new OrderInfo("ORD004", "iPad Air", "处理中", "2024-01-20", "订单处理中,预计今日发货"));
    }

    public OrderQueryTool(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @PostConstruct
    public void register() {
        toolRegistry.registerTool(this);
        log.info("OrderQueryTool registered");
    }

    @Override
    public String getName() {
        return "order-query";
    }

    @Override
    public String getDescription() {
        return "Query order information by order ID. Input should be an order ID (e.g., ORD001)";
    }

    @Override
    public ToolResult execute(String input) {
        try {
            String orderId = input.trim().toUpperCase();
            log.debug("Querying order: {}", orderId);

            OrderInfo orderInfo = ORDER_DATABASE.get(orderId);

            if (orderInfo == null) {
                return ToolResult.success("订单号 " + orderId + " 不存在。请检查订单号是否正确。");
            }

            String result = String.format(
                    "订单信息:\n" +
                    "订单号: %s\n" +
                    "商品: %s\n" +
                    "状态: %s\n" +
                    "下单时间: %s\n" +
                    "物流信息: %s",
                    orderInfo.orderId,
                    orderInfo.product,
                    orderInfo.status,
                    orderInfo.orderDate,
                    orderInfo.logistics
            );

            return ToolResult.success(result);

        } catch (Exception e) {
            log.error("Error querying order", e);
            return ToolResult.failure("查询订单时出错: " + e.getMessage());
        }
    }

    @Override
    public String getParameterSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "order_id": {
                      "type": "string",
                      "description": "订单号"
                    }
                  },
                  "required": ["order_id"]
                }
                """;
    }

    private static class OrderInfo {
        String orderId;
        String product;
        String status;
        String orderDate;
        String logistics;

        OrderInfo(String orderId, String product, String status, String orderDate, String logistics) {
            this.orderId = orderId;
            this.product = product;
            this.status = status;
            this.orderDate = orderDate;
            this.logistics = logistics;
        }
    }
}
