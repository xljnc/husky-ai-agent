package com.wt.test.husky.ai.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Search Client 启动类
 * 基于Spring AI + WebFlux实现，集成AnySearch MCP搜索能力，对外提供SSE流式接口
 */
@SpringBootApplication
public class HuskyAiAgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HuskyAiAgentApplication.class, args);
    }
}
