package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 配置 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class AgentConfigVO {
    private Long agentId;
    private Long workspaceId;
    private String name;
    private String description;
    private String avatarUrl;
    /** REACT/CHAIN/ROUTE/PLANNER_CRITIC/SUPERVISOR_WORKER */
    private String executionMode;
    private Long modelConfigId;
    /** 绑定的模型名称（冗余展示） */
    private String modelName;
    private String systemPrompt;
    private Integer maxSteps;
    private Integer timeoutSeconds;
    private Boolean parallelEnabled;
    /** 1=DRAFT 2=ACTIVE 3=ARCHIVED */
    private Integer status;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility;
    private Integer version;
    /** 绑定的工具列表 */
    private List<ToolConfigVO> tools;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
