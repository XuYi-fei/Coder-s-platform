package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.AgentConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.AgentConfigVO;
import com.github.paicoding.forum.api.model.vo.agent.ToolConfigVO;
import com.github.paicoding.forum.service.agent.repository.dao.AgentConfigDao;
import com.github.paicoding.forum.service.agent.repository.dao.ToolConfigDao;
import com.github.paicoding.forum.service.agent.repository.entity.AgentConfigDO;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigDao agentConfigDao;
    private final ToolConfigDao toolConfigDao;

    public List<AgentConfigVO> listAgents(Long workspaceId) {
        return agentConfigDao.listByWorkspace(workspaceId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public AgentConfigVO getAgent(Long agentId) {
        AgentConfigDO agent = agentConfigDao.getById(agentId);
        if (agent == null) return null;
        AgentConfigVO vo = toVO(agent);
        // Load tools
        List<ToolConfigDO> tools = toolConfigDao.listByAgentId(agentId);
        vo.setTools(tools.stream().map(this::toolToVO).collect(Collectors.toList()));
        return vo;
    }

    public Long createAgent(AgentConfigReqVO req, Long creatorUserId) {
        AgentConfigDO agent = new AgentConfigDO();
        agent.setWorkspaceId(req.getWorkspaceId());
        agent.setName(req.getName());
        agent.setDescription(req.getDescription());
        agent.setAvatarUrl(req.getAvatarUrl());
        agent.setExecutionMode(req.getExecutionMode());
        agent.setModelConfigId(req.getModelConfigId());
        agent.setSystemPrompt(req.getSystemPrompt());
        agent.setMaxSteps(req.getMaxSteps());
        agent.setTimeoutSeconds(req.getTimeoutSeconds());
        agent.setParallelEnabled(req.getParallelEnabled() != null && req.getParallelEnabled() ? 1 : 0);
        agent.setVisibility(req.getVisibility());
        agent.setStatus(1); // DRAFT
        agent.setIsSystem(0);
        agent.setVersion(1);
        agent.setCreatorUserId(creatorUserId);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        Long agentId = agentConfigDao.save(agent);

        // Bind tools
        if (!CollectionUtils.isEmpty(req.getToolIds())) {
            toolConfigDao.bindTools(agentId, req.getToolIds());
        }
        return agentId;
    }

    public boolean updateAgent(Long agentId, AgentConfigReqVO req) {
        AgentConfigDO agent = agentConfigDao.getById(agentId);
        if (agent == null) return false;
        if (req.getName() != null) agent.setName(req.getName());
        if (req.getDescription() != null) agent.setDescription(req.getDescription());
        if (req.getSystemPrompt() != null) agent.setSystemPrompt(req.getSystemPrompt());
        if (req.getModelConfigId() != null) agent.setModelConfigId(req.getModelConfigId());
        if (req.getMaxSteps() != null) agent.setMaxSteps(req.getMaxSteps());
        if (req.getVisibility() != null) agent.setVisibility(req.getVisibility());
        agent.setUpdatedAt(LocalDateTime.now());
        boolean updated = agentConfigDao.updateById(agent);
        if (updated && req.getToolIds() != null) {
            toolConfigDao.bindTools(agentId, req.getToolIds());
        }
        return updated;
    }

    public boolean activateAgent(Long agentId) {
        AgentConfigDO agent = agentConfigDao.getById(agentId);
        if (agent == null) return false;
        agent.setStatus(2); // ACTIVE
        agent.setUpdatedAt(LocalDateTime.now());
        return agentConfigDao.updateById(agent);
    }

    public boolean deleteAgent(Long agentId) {
        AgentConfigDO agent = agentConfigDao.getById(agentId);
        if (agent == null || agent.getIsSystem() == 1) return false;
        agent.setStatus(3); // ARCHIVED
        agent.setUpdatedAt(LocalDateTime.now());
        return agentConfigDao.updateById(agent);
    }

    private AgentConfigVO toVO(AgentConfigDO agent) {
        AgentConfigVO vo = new AgentConfigVO();
        vo.setAgentId(agent.getAgentId());
        vo.setWorkspaceId(agent.getWorkspaceId());
        vo.setName(agent.getName());
        vo.setDescription(agent.getDescription());
        vo.setAvatarUrl(agent.getAvatarUrl());
        vo.setExecutionMode(agent.getExecutionMode());
        vo.setModelConfigId(agent.getModelConfigId());
        vo.setSystemPrompt(agent.getSystemPrompt());
        vo.setMaxSteps(agent.getMaxSteps());
        vo.setTimeoutSeconds(agent.getTimeoutSeconds());
        vo.setParallelEnabled(agent.getParallelEnabled() != null && agent.getParallelEnabled() == 1);
        vo.setStatus(agent.getStatus());
        vo.setVisibility(agent.getVisibility());
        vo.setVersion(agent.getVersion());
        vo.setCreatedAt(agent.getCreatedAt());
        vo.setUpdatedAt(agent.getUpdatedAt());
        return vo;
    }

    private ToolConfigVO toolToVO(ToolConfigDO tool) {
        ToolConfigVO vo = new ToolConfigVO();
        vo.setToolId(tool.getToolId());
        vo.setName(tool.getName());
        vo.setDisplayName(tool.getDisplayName());
        vo.setDescription(tool.getDescription());
        vo.setToolType(tool.getToolType());
        vo.setInputSchema(tool.getInputSchema());
        vo.setConfig(tool.getConfig());
        vo.setTimeoutMs(tool.getTimeoutMs());
        vo.setRiskLevel(tool.getRiskLevel());
        vo.setStatus(tool.getStatus());
        return vo;
    }
}
