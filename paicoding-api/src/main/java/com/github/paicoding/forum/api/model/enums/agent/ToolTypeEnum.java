package com.github.paicoding.forum.api.model.enums.agent;

import lombok.Getter;

/**
 * 工具类型枚举
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Getter
public enum ToolTypeEnum {
    HTTP_API("HTTP API", "调用外部 HTTP 接口"),
    MCP("MCP", "通过 MCP 协议调用工具"),
    AGENT_DELEGATE("Agent 委托", "委托另一个 Agent 执行子任务"),
    JAVA_FUNCTION("Java 函数", "调用 Spring Bean 中的方法"),
    SCRIPT("脚本", "执行 Python/JS 等脚本"),
    DATABASE_QUERY("数据库查询", "执行只读 SQL 查询");

    private final String displayName;
    private final String description;

    ToolTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
