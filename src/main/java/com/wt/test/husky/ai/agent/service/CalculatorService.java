package com.wt.test.husky.ai.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MCP 计算工具服务
 * 提供基础四则运算、幂运算、开方等数学计算能力
 * 所有方法使用@Tool注解注册为MCP工具，自动生成JSON Schema
 */
@Slf4j
@Service
public class CalculatorService {
    
    private static final int SCALE = 10;
    
    /**
     * 加法计算
     */
    @Tool(name = "add", description = "计算两个数字的加法结果，返回a + b的值")
    public Mono<Double> add(
            @ToolParam(description = "第一个加数", required = true) double a,
            @ToolParam(description = "第二个加数", required = true) double b) {
        log.info("执行加法: {} + {}", a, b);
        return Mono.just(BigDecimal.valueOf(a)
                .add(BigDecimal.valueOf(b))
                .doubleValue());
    }
    
    /**
     * 减法计算
     */
    @Tool(name = "subtract", description = "计算两个数字的减法结果，返回a - b的值")
    public Mono<Double> subtract(
            @ToolParam(description = "被减数", required = true) double a,
            @ToolParam(description = "减数", required = true) double b) {
        log.info("执行减法: {} - {}", a, b);
        return Mono.just(BigDecimal.valueOf(a)
                .subtract(BigDecimal.valueOf(b))
                .doubleValue());
    }
    
    /**
     * 乘法计算
     */
    @Tool(name = "multiply", description = "计算两个数字的乘法结果，返回a * b的值")
    public Mono<Double> multiply(
            @ToolParam(description = "第一个乘数", required = true) double a,
            @ToolParam(description = "第二个乘数", required = true) double b) {
        log.info("执行乘法: {} * {}", a, b);
        return Mono.just(BigDecimal.valueOf(a)
                .multiply(BigDecimal.valueOf(b))
                .doubleValue());
    }
    
    /**
     * 除法计算（带除零保护）
     */
    @Tool(name = "divide", description = "计算两个数字的除法结果，返回a / b的值。注意b不能为0")
    public Mono<String> divide(
            @ToolParam(description = "被除数", required = true) double a,
            @ToolParam(description = "除数，不能为0", required = true) double b) {
        log.info("执行除法: {} / {}", a, b);
        return Mono.fromSupplier(() -> {
            if (Math.abs(b) < 1e-12) {
                return "错误：除数不能为0";
            }
            double result = BigDecimal.valueOf(a)
                    .divide(BigDecimal.valueOf(b), SCALE, RoundingMode.HALF_UP)
                    .doubleValue();
            return String.format("计算结果: %.6f", result);
        });
    }
    
    /**
     * 幂运算
     */
    @Tool(name = "power", description = "计算幂运算，返回base的exponent次方的值，即base^exponent")
    public Mono<Double> power(
            @ToolParam(description = "底数", required = true) double base,
            @ToolParam(description = "指数", required = true) double exponent) {
        log.info("执行幂运算: {} ^ {}", base, exponent);
        return Mono.just(Math.pow(base, exponent));
    }
    
    /**
     * 平方根计算
     */
    @Tool(name = "sqrt", description = "计算一个非负数的平方根。注意输入不能为负数")
    public Mono<String> sqrt(
            @ToolParam(description = "需要开平方的非负数", required = true) double number) {
        log.info("执行平方根: √{}", number);
        return Mono.fromSupplier(() -> {
            if (number < 0) {
                return "错误：不能对负数进行开平方运算";
            }
            double result = Math.sqrt(number);
            return String.format("√%.4f = %.6f", number, result);
        });
    }
    
    /**
     * 取模运算
     */
    @Tool(name = "modulo", description = "计算取模（求余）运算，返回a除以b的余数，即a mod b。注意b不能为0")
    public Mono<String> modulo(
            @ToolParam(description = "被除数", required = true) int a,
            @ToolParam(description = "除数，不能为0", required = true) int b) {
        log.info("执行取模: {} mod {}", a, b);
        return Mono.fromSupplier(() -> {
            if (b == 0) {
                return "错误：除数不能为0";
            }
            int result = a % b;
            return String.format("%d mod %d = %d", a, b, result);
        });
    }
}
