package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.AgentSendReqVO;
import com.github.paicoding.forum.api.model.vo.agent.AgentSessionVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.AgentExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Agent 执行入口 REST 控制器
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Slf4j
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class AgentRestController {

    private final AgentExecutionService agentExecutionService;

    /**
     * 向 Agent 发送消息（SSE 流式响应）
     * 每个 SSE 数据块是一个 JSON 字符串，对应一个 AgentStreamEvent
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "/send", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> send(@RequestBody AgentSendReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        log.info("Agent send: agentId={}, userId={}, message={}", req.getAgentId(), userId, req.getMessage());
        return agentExecutionService.executeStreaming(req.getAgentId(), userId, req.getMessage())
                .map(event -> event + "\n");
    }

    /**
     * 获取当前用户的历史会话列表
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/sessions")
    public ResVo<List<AgentSessionVO>> listSessions(
            @RequestParam(required = false) Long agentId) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(agentExecutionService.listSessions(userId, agentId));
    }

    /**
     * 获取单次会话详情（含步骤）
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/session/{sessionId}")
    public ResVo<AgentSessionVO> getSession(@PathVariable Long sessionId) {
        return ResVo.ok(agentExecutionService.getSession(sessionId));
    }
}
