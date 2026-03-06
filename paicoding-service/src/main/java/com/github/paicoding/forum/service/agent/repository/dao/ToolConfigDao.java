package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.AgentToolRelDO;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import com.github.paicoding.forum.service.agent.repository.mapper.AgentToolRelMapper;
import com.github.paicoding.forum.service.agent.repository.mapper.ToolConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具配置 DAO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Repository
@RequiredArgsConstructor
public class ToolConfigDao {

    private final ToolConfigMapper toolConfigMapper;
    private final AgentToolRelMapper agentToolRelMapper;

    public ToolConfigDO getById(Long toolId) {
        return toolConfigMapper.selectById(toolId);
    }

    public List<ToolConfigDO> listByWorkspace(Long workspaceId) {
        return toolConfigMapper.selectList(new LambdaQueryWrapper<ToolConfigDO>()
                .eq(ToolConfigDO::getWorkspaceId, workspaceId)
                .eq(ToolConfigDO::getStatus, 1)
                .orderByAsc(ToolConfigDO::getToolId));
    }

    /**
     * 查询某 Agent 绑定的所有已启用工具（按 sort_order 排序）
     */
    public List<ToolConfigDO> listByAgentId(Long agentId) {
        List<AgentToolRelDO> rels = agentToolRelMapper.selectList(
                new LambdaQueryWrapper<AgentToolRelDO>()
                        .eq(AgentToolRelDO::getAgentId, agentId)
                        .eq(AgentToolRelDO::getIsEnabled, 1)
                        .orderByAsc(AgentToolRelDO::getSortOrder));
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> toolIds = rels.stream().map(AgentToolRelDO::getToolId).collect(Collectors.toList());
        return toolConfigMapper.selectList(new LambdaQueryWrapper<ToolConfigDO>()
                .in(ToolConfigDO::getToolId, toolIds)
                .eq(ToolConfigDO::getStatus, 1));
    }

    public Long save(ToolConfigDO toolConfigDO) {
        toolConfigMapper.insert(toolConfigDO);
        return toolConfigDO.getToolId();
    }

    public boolean updateById(ToolConfigDO toolConfigDO) {
        return toolConfigMapper.updateById(toolConfigDO) > 0;
    }

    public boolean removeById(Long toolId) {
        return toolConfigMapper.deleteById(toolId) > 0;
    }

    // ---- Agent-Tool 关联操作 ----

    public void bindTools(Long agentId, List<Long> toolIds) {
        // 先清除旧绑定
        agentToolRelMapper.delete(new LambdaQueryWrapper<AgentToolRelDO>()
                .eq(AgentToolRelDO::getAgentId, agentId));
        // 重新插入
        for (int i = 0; i < toolIds.size(); i++) {
            AgentToolRelDO rel = new AgentToolRelDO();
            rel.setAgentId(agentId);
            rel.setToolId(toolIds.get(i));
            rel.setSortOrder(i);
            rel.setIsEnabled(1);
            agentToolRelMapper.insert(rel);
        }
    }
}
