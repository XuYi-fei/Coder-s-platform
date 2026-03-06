package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 执行会话 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class AgentSessionVO {
    private Long sessionId;
    private Long agentId;
    private String agentName;
    private String userInput;
    private String finalOutput;
    /** PLANNING/EXECUTING/COMPLETED/FAILED */
    private String status;
    private Integer tokensTotal;
    private List<ExecutionStepVO> steps;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
