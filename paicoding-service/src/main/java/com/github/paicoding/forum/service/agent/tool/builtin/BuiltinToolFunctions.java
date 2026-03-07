package com.github.paicoding.forum.service.agent.tool.builtin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 内置工具函数集合，供 JAVA_FUNCTION 类型的工具调用
 * Bean 名称: builtinToolFunctions
 * 每个方法签名必须为 (String input) -> String
 */
@Slf4j
@Component("builtinToolFunctions")
@RequiredArgsConstructor
public class BuiltinToolFunctions {

    private final ObjectMapper objectMapper;
    private static final ExpressionParser SPEL = new SpelExpressionParser();

    /**
     * 获取当前日期和时间
     * config: {"bean_name":"builtinToolFunctions","method_name":"getCurrentTime"}
     */
    public String getCurrentTime(String input) {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "当前时间：" + now;
    }

    /**
     * 计算数学表达式
     * input JSON: {"expression": "2 + 3 * 4"}
     * config: {"bean_name":"builtinToolFunctions","method_name":"calculate"}
     */
    public String calculate(String input) {
        try {
            JsonNode node = objectMapper.readTree(input);
            String expression = node.path("expression").asText();
            if (expression.isEmpty()) {
                return "错误：expression 字段不能为空";
            }
            // Use SpEL to evaluate — supports +,-,*,/,%,^, Math functions
            Object result = SPEL.parseExpression(expression).getValue();
            return "计算结果：" + expression + " = " + result;
        } catch (Exception e) {
            log.warn("Calculate failed for input: {}, error: {}", input, e.getMessage());
            return "计算失败：" + e.getMessage();
        }
    }
}
