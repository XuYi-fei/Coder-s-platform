package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agent 执行会话实体（对应 agent_session 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("agent_session")
public class AgentSessionDO {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    private Long workspaceId;
    private Long entryAgentId;
    private Long userId;
    private String userInput;
    private String finalOutput;
    /** PLANNING/EXECUTING/COMPLETED/FAILED */
    private String status;
    private Integer tokensTotal;
    private BigDecimal costUsd;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
