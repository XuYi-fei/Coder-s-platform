package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行步骤 VO（用于前端展示 ReAct 思考过程）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class ExecutionStepVO {
    private Long stepId;
    private Long sessionId;
    private Integer stepOrder;
    /** 步骤类型: THOUGHT/ACTION/OBSERVATION/ANSWER */
    private String stepType;
    private String stepDesc;
    /** 调用的工具名称（ACTION 类型时有值） */
    private String toolName;
    /** 入参（JSON 字符串） */
    private String inputParams;
    /** 输出结果（JSON 字符串） */
    private String outputResult;
    /** PENDING/RUNNING/COMPLETED/FAILED */
    private String status;
    private Integer tokensUsed;
    private Integer durationMs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
