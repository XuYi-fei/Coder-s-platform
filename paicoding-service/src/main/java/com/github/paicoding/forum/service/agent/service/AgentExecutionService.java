package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.AgentSessionVO;
import com.github.paicoding.forum.api.model.vo.agent.ExecutionStepVO;
import com.github.paicoding.forum.service.agent.engine.ReActAgentEngine;
import com.github.paicoding.forum.service.agent.repository.dao.AgentConfigDao;
import com.github.paicoding.forum.service.agent.repository.dao.AgentSessionDao;
import com.github.paicoding.forum.service.agent.repository.dao.ExecutionStepDao;
import com.github.paicoding.forum.service.agent.repository.dao.ToolConfigDao;
import com.github.paicoding.forum.service.agent.repository.entity.AgentConfigDO;
import com.github.paicoding.forum.service.agent.repository.entity.AgentSessionDO;
import com.github.paicoding.forum.service.agent.repository.entity.ExecutionStepDO;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentExecutionService {

    private final AgentConfigDao agentConfigDao;
    private final AgentSessionDao agentSessionDao;
    private final ExecutionStepDao executionStepDao;
    private final ToolConfigDao toolConfigDao;
    private final ModelConfigService modelConfigService;
    private final ReActAgentEngine reActAgentEngine;

    /**
     * 创建新会话并执行 Agent（流式）
     *
     * @param agentId Agent ID
     * @param userId  当前用户 ID
     * @param message 用户输入
     * @return SSE 事件流（每个元素是 JSON 字符串）
     */
    public Flux<String> executeStreaming(Long agentId, Long userId, String message) {
        // 1. 加载 Agent 配置
        AgentConfigDO agentConfig = agentConfigDao.getById(agentId);
        if (agentConfig == null) {
            return Flux.just("{\"type\":\"ERROR\",\"content\":\"Agent not found: " + agentId + "\"}");
        }
        if (agentConfig.getStatus() != 2) {
            return Flux.just("{\"type\":\"ERROR\",\"content\":\"Agent is not active\"}");
        }

        // 2. 创建 Session
        AgentSessionDO session = new AgentSessionDO();
        session.setWorkspaceId(agentConfig.getWorkspaceId());
        session.setEntryAgentId(agentId);
        session.setUserId(userId);
        session.setUserInput(message);
        session.setStatus("EXECUTING");
        session.setTokensTotal(0);
        session.setStartedAt(LocalDateTime.now());
        Long sessionId = agentSessionDao.save(session);

        // 3. 加载 ChatClient
        ChatClient chatClient;
        try {
            chatClient = modelConfigService.buildChatClient(agentConfig.getModelConfigId());
        } catch (Exception e) {
            agentSessionDao.updateStatus(sessionId, "FAILED", null);
            return Flux.just("{\"type\":\"ERROR\",\"content\":\"Failed to load model: " + e.getMessage() + "\"}");
        }

        // 4. 加载工具列表
        List<ToolConfigDO> tools = toolConfigDao.listByAgentId(agentId);
        log.info("Agent {} executing with {} tools, sessionId={}", agentId, tools.size(), sessionId);

        // 5. 运行 ReAct 引擎
        return reActAgentEngine.run(chatClient, agentConfig, tools, sessionId, message)
                .doOnComplete(() -> agentSessionDao.updateStatus(sessionId, "COMPLETED", null))
                .doOnError(e -> {
                    log.error("Agent execution failed, sessionId={}: {}", sessionId, e.getMessage());
                    agentSessionDao.updateStatus(sessionId, "FAILED", null);
                });
    }

    /**
     * 获取历史会话列表
     */
    public List<AgentSessionVO> listSessions(Long userId, Long agentId) {
        return agentSessionDao.listByUser(userId, agentId).stream()
                .map(this::sessionToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个会话详情（含步骤）
     */
    public AgentSessionVO getSession(Long sessionId) {
        AgentSessionDO session = agentSessionDao.getById(sessionId);
        if (session == null) return null;
        AgentSessionVO vo = sessionToVO(session);
        List<ExecutionStepDO> steps = executionStepDao.listBySession(sessionId);
        vo.setSteps(steps.stream().map(this::stepToVO).collect(Collectors.toList()));
        return vo;
    }

    private AgentSessionVO sessionToVO(AgentSessionDO session) {
        AgentSessionVO vo = new AgentSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setAgentId(session.getEntryAgentId());
        vo.setUserInput(session.getUserInput());
        vo.setFinalOutput(session.getFinalOutput());
        vo.setStatus(session.getStatus());
        vo.setTokensTotal(session.getTokensTotal());
        vo.setStartedAt(session.getStartedAt());
        vo.setCompletedAt(session.getCompletedAt());
        return vo;
    }

    private ExecutionStepVO stepToVO(ExecutionStepDO step) {
        ExecutionStepVO vo = new ExecutionStepVO();
        vo.setStepId(step.getStepId());
        vo.setSessionId(step.getSessionId());
        vo.setStepOrder(step.getStepOrder());
        vo.setStepDesc(step.getStepDesc());
        vo.setInputParams(step.getInputParams());
        vo.setOutputResult(step.getOutputResult());
        vo.setStatus(step.getStatus());
        vo.setTokensUsed(step.getTokensUsed());
        vo.setDurationMs(step.getDurationMs());
        vo.setStartedAt(step.getStartedAt());
        vo.setCompletedAt(step.getCompletedAt());
        return vo;
    }
}
