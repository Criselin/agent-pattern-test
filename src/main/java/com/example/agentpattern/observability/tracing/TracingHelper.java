package com.example.agentpattern.observability.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 追踪辅助类 - 存根实现
 *
 * NOTE: This is a stub implementation as Langfuse SDK is not available in Maven Central.
 */
@Slf4j
@Component
public class TracingHelper {

    public TracingHelper() {
        log.info("TracingHelper initialized (stub mode - Langfuse disabled)");
    }

    // 添加任何需要的辅助方法,作为空操作
}
