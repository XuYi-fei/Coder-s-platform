package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

/**
 * 发送消息给 Agent 请求 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class AgentSendReqVO {
    /** Agent ID */
    private Long agentId;
    /** 会话 ID（空则创建新会话） */
    private Long sessionId;
    /** 用户输入内容 */
    private String message;
}
