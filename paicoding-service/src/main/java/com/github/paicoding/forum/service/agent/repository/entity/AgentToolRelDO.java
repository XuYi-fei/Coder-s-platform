package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 与 Tool 关联实体（对应 agent_tool_rel 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("agent_tool_rel")
public class AgentToolRelDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long agentId;
    private Long toolId;
    private Integer sortOrder;
    private Integer isEnabled;
    /** Agent 级别对 Tool 配置的覆盖（JSON） */
    private String overrideConfig;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
