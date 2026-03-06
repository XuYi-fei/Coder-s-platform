package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具配置实体（对应 tool_config 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("tool_config")
public class ToolConfigDO {
    @TableId(value = "tool_id", type = IdType.AUTO)
    private Long toolId;

    private Long workspaceId;
    /** 工具名（英文下划线，LLM 识别用） */
    private String name;
    private String displayName;
    private String description;
    /** HTTP_API/MCP/AGENT_DELEGATE/JAVA_FUNCTION/SCRIPT/DATABASE_QUERY */
    private String toolType;
    /** 入参 JSON Schema */
    private String inputSchema;
    /** 出参 JSON Schema */
    private String outputSchema;
    /** 执行配置（JSON） */
    private String config;
    private Integer timeoutMs;
    private Integer retryCount;
    private Integer retryIntervalMs;
    /** 1=READ 2=MUTATION 3=DANGEROUS */
    private Integer riskLevel;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED */
    private Integer visibility;
    /** 1=正常 2=禁用 */
    private Integer status;
    private Integer isSystem;
    private Long creatorUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
