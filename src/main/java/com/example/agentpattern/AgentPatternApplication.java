package com.example.agentpattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Agent Pattern Test Application
 * 用于测试各种Agent设计模式的Spring Boot应用
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class AgentPatternApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AgentPatternApplication.class, args);
        logApplicationStartup(context.getEnvironment());
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info("""

                ----------------------------------------------------------
                Application '{}' is running! Access URLs:
                Local:      {}://localhost:{}{}
                External:   {}://{}:{}{}
                Profile(s): {}
                ----------------------------------------------------------
                API Documentation:
                Chat API:      POST   {}://localhost:{}/api/chat
                Welcome:       GET    {}://localhost:{}/api/chat/welcome
                Health Check:  GET    {}://localhost:{}/api/chat/health
                Stats:         GET    {}://localhost:{}/api/chat/stats
                ----------------------------------------------------------
                """,
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles(),
                protocol,
                serverPort,
                protocol,
                serverPort,
                protocol,
                serverPort,
                protocol,
                serverPort
        );
    }
}
