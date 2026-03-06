package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 配置实体（对应 agent_config 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("agent_config")
public class AgentConfigDO {
    @TableId(value = "agent_id", type = IdType.AUTO)
    private Long agentId;

    private Long workspaceId;
    private String name;
    private String description;
    private String avatarUrl;
    /** REACT/CHAIN/ROUTE/PLANNER_CRITIC/SUPERVISOR_WORKER */
    private String executionMode;
    private Long modelConfigId;
    private Long criticModelConfigId;
    private String systemPrompt;
    private String plannerPrompt;
    private String criticPrompt;
    private String criticScoreConfig;
    private Integer criticPassScore;
    private Integer maxIterations;
    private Integer maxSteps;
    private Integer timeoutSeconds;
    /** 0=否 1=是 */
    private Integer parallelEnabled;
    private Integer humanReviewOnMutation;
    private Integer humanReviewOnUncertain;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED */
    private Integer visibility;
    /** 1=DRAFT 2=ACTIVE 3=ARCHIVED */
    private Integer status;
    private Integer isSystem;
    private Integer version;
    private Long creatorUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
