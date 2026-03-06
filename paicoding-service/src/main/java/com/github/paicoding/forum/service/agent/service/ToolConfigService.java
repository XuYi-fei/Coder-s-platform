package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.ToolConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.ToolConfigVO;
import com.github.paicoding.forum.service.agent.repository.dao.ToolConfigDao;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolConfigService {

    private final ToolConfigDao toolConfigDao;

    public List<ToolConfigVO> listTools(Long workspaceId) {
        return toolConfigDao.listByWorkspace(workspaceId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public ToolConfigVO getTool(Long toolId) {
        ToolConfigDO tool = toolConfigDao.getById(toolId);
        return tool != null ? toVO(tool) : null;
    }

    public Long createTool(ToolConfigReqVO req, Long creatorUserId) {
        ToolConfigDO tool = new ToolConfigDO();
        tool.setWorkspaceId(req.getWorkspaceId());
        tool.setName(req.getName());
        tool.setDisplayName(req.getDisplayName());
        tool.setDescription(req.getDescription());
        tool.setToolType(req.getToolType());
        tool.setInputSchema(req.getInputSchema());
        tool.setConfig(req.getConfig());
        tool.setTimeoutMs(req.getTimeoutMs());
        tool.setRetryCount(req.getRetryCount());
        tool.setRetryIntervalMs(1000);
        tool.setRiskLevel(req.getRiskLevel());
        tool.setVisibility(req.getVisibility());
        tool.setStatus(1);
        tool.setIsSystem(0);
        tool.setCreatorUserId(creatorUserId);
        tool.setCreatedAt(LocalDateTime.now());
        tool.setUpdatedAt(LocalDateTime.now());
        return toolConfigDao.save(tool);
    }

    public boolean updateTool(Long toolId, ToolConfigReqVO req) {
        ToolConfigDO tool = toolConfigDao.getById(toolId);
        if (tool == null) return false;
        if (req.getName() != null) tool.setName(req.getName());
        if (req.getDisplayName() != null) tool.setDisplayName(req.getDisplayName());
        if (req.getDescription() != null) tool.setDescription(req.getDescription());
        if (req.getInputSchema() != null) tool.setInputSchema(req.getInputSchema());
        if (req.getConfig() != null) tool.setConfig(req.getConfig());
        if (req.getTimeoutMs() != null) tool.setTimeoutMs(req.getTimeoutMs());
        if (req.getRiskLevel() != null) tool.setRiskLevel(req.getRiskLevel());
        tool.setUpdatedAt(LocalDateTime.now());
        return toolConfigDao.updateById(tool);
    }

    public boolean deleteTool(Long toolId) {
        ToolConfigDO tool = toolConfigDao.getById(toolId);
        if (tool == null || tool.getIsSystem() == 1) return false;
        tool.setStatus(2); // DISABLED
        tool.setUpdatedAt(LocalDateTime.now());
        return toolConfigDao.updateById(tool);
    }

    private ToolConfigVO toVO(ToolConfigDO tool) {
        ToolConfigVO vo = new ToolConfigVO();
        vo.setToolId(tool.getToolId());
        vo.setWorkspaceId(tool.getWorkspaceId());
        vo.setName(tool.getName());
        vo.setDisplayName(tool.getDisplayName());
        vo.setDescription(tool.getDescription());
        vo.setToolType(tool.getToolType());
        vo.setInputSchema(tool.getInputSchema());
        vo.setConfig(tool.getConfig());
        vo.setTimeoutMs(tool.getTimeoutMs());
        vo.setRetryCount(tool.getRetryCount());
        vo.setRiskLevel(tool.getRiskLevel());
        vo.setVisibility(tool.getVisibility());
        vo.setStatus(tool.getStatus());
        vo.setCreatedAt(tool.getCreatedAt());
        return vo;
    }
}
