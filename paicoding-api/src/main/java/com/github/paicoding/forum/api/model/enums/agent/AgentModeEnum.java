package com.github.paicoding.forum.api.model.enums.agent;

import lombok.Getter;

/**
 * Agent 执行模式枚举
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Getter
public enum AgentModeEnum {
    REACT("ReAct", "一边推理一边行动，动态决策调用工具"),
    CHAIN("Chain", "固定工具调用链，顺序执行"),
    ROUTE("Route", "路由模式，根据输入选择不同子 Agent"),
    PLANNER_CRITIC("Planner-Critic", "先规划再执行，Critic 评审计划后执行"),
    SUPERVISOR_WORKER("Supervisor-Worker", "主 Agent 监督多个 Worker Agent 并行执行");

    private final String displayName;
    private final String description;

    AgentModeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static AgentModeEnum of(String name) {
        for (AgentModeEnum mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return REACT;
    }
}
