package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行步骤实体（对应 execution_step 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("execution_step")
public class ExecutionStepDO {
    @TableId(value = "step_id", type = IdType.AUTO)
    private Long stepId;

    private Long planId;
    private Long sessionId;
    private Integer stepOrder;
    private String dependsOn;
    private Long agentId;
    private Long toolId;
    private Long skillId;
    private String stepDesc;
    /** 步骤类型: THOUGHT/ACTION/OBSERVATION/ANSWER（ReAct 专用扩展字段） */
    @TableField(exist = false)
    private String stepType;
    private String inputParams;
    private String outputResult;
    private String errorMessage;
    /** PENDING/RUNNING/COMPLETED/FAILED/SKIPPED/HUMAN_PENDING */
    private String status;
    private Integer tokensUsed;
    private Integer durationMs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
