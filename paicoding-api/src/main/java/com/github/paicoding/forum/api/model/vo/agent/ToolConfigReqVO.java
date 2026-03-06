package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

/**
 * 创建/更新工具配置请求 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class ToolConfigReqVO {
    private Long workspaceId;
    /** 工具名（英文下划线，LLM 通过此字段识别工具） */
    private String name;
    private String displayName;
    private String description;
    /** HTTP_API/MCP/AGENT_DELEGATE/JAVA_FUNCTION/SCRIPT/DATABASE_QUERY */
    private String toolType;
    /** 入参 JSON Schema（JSON 字符串） */
    private String inputSchema;
    /** 各类型执行配置（JSON 字符串） */
    private String config;
    private Integer timeoutMs = 10000;
    private Integer retryCount = 0;
    /** 1=READ 2=MUTATION 3=DANGEROUS */
    private Integer riskLevel = 1;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility = 1;
}
