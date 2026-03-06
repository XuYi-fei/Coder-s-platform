package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.util.List;

/**
 * 创建/更新 Agent 配置请求 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class AgentConfigReqVO {
    private Long workspaceId;
    private String name;
    private String description;
    private String avatarUrl;
    /** REACT/CHAIN/ROUTE/PLANNER_CRITIC/SUPERVISOR_WORKER */
    private String executionMode = "REACT";
    private Long modelConfigId;
    private String systemPrompt;
    private Integer maxSteps = 10;
    private Integer timeoutSeconds = 120;
    private Boolean parallelEnabled = false;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility = 1;
    /** 绑定的工具 ID 列表 */
    private List<Long> toolIds;
}
