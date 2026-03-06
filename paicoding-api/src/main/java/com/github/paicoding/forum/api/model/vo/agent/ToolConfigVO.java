package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具配置 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class ToolConfigVO {
    private Long toolId;
    private Long workspaceId;
    /** 工具名称（供 LLM 识别，英文下划线格式） */
    private String name;
    /** 显示名称 */
    private String displayName;
    /** 工具功能描述 */
    private String description;
    /** HTTP_API/MCP/AGENT_DELEGATE/JAVA_FUNCTION/SCRIPT/DATABASE_QUERY */
    private String toolType;
    /** 入参 JSON Schema */
    private String inputSchema;
    /** 执行配置（JSON） */
    private String config;
    private Integer timeoutMs;
    private Integer retryCount;
    /** 1=READ 2=MUTATION 3=DANGEROUS */
    private Integer riskLevel;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility;
    /** 1=正常 2=禁用 */
    private Integer status;
    private LocalDateTime createdAt;
}
