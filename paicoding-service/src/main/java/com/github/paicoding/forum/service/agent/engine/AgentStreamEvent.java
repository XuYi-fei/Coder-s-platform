package com.github.paicoding.forum.service.agent.engine;

import lombok.Builder;
import lombok.Data;

/**
 * Agent SSE 流事件
 * 序列化为 JSON 字符串后通过 SSE 推送给前端
 */
@Data
@Builder
public class AgentStreamEvent {
    /** THOUGHT / ACTION / OBSERVATION / ANSWER / DONE / ERROR / HEARTBEAT */
    private String type;
    /** 步骤序号 */
    private Integer stepOrder;
    /** 内容 */
    private String content;
    /** 工具名称（ACTION 时有值） */
    private String toolName;
    /** 是否为最终回答 */
    private Boolean done;
    /** 会话 ID */
    private Long sessionId;

    public static AgentStreamEvent heartbeat() {
        return AgentStreamEvent.builder().type("HEARTBEAT").done(false).build();
    }

    public static AgentStreamEvent done(Long sessionId) {
        return AgentStreamEvent.builder().type("DONE").sessionId(sessionId).done(true).build();
    }

    public static AgentStreamEvent error(String message) {
        return AgentStreamEvent.builder().type("ERROR").content(message).done(true).build();
    }

    public static AgentStreamEvent thought(int step, String content) {
        return AgentStreamEvent.builder().type("THOUGHT").stepOrder(step).content(content).done(false).build();
    }

    public static AgentStreamEvent action(int step, String toolName, String input) {
        return AgentStreamEvent.builder().type("ACTION").stepOrder(step).toolName(toolName).content(input).done(false).build();
    }

    public static AgentStreamEvent observation(int step, String result) {
        return AgentStreamEvent.builder().type("OBSERVATION").stepOrder(step).content(result).done(false).build();
    }

    public static AgentStreamEvent answer(int step, String content) {
        return AgentStreamEvent.builder().type("ANSWER").stepOrder(step).content(content).done(false).build();
    }
}
