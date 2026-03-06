package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.AgentConfigDO;
import com.github.paicoding.forum.service.agent.repository.mapper.AgentConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Agent 配置 DAO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Repository
@RequiredArgsConstructor
public class AgentConfigDao {

    private final AgentConfigMapper agentConfigMapper;

    public AgentConfigDO getById(Long agentId) {
        return agentConfigMapper.selectById(agentId);
    }

    public List<AgentConfigDO> listByWorkspace(Long workspaceId) {
        return agentConfigMapper.selectList(new LambdaQueryWrapper<AgentConfigDO>()
                .eq(AgentConfigDO::getWorkspaceId, workspaceId)
                .ne(AgentConfigDO::getStatus, 3) // exclude archived
                .orderByAsc(AgentConfigDO::getAgentId));
    }

    /**
     * 查询工作区可用 Agent（含公开 Agent）
     */
    public List<AgentConfigDO> listActiveForWorkspace(Long workspaceId) {
        return agentConfigMapper.selectList(new LambdaQueryWrapper<AgentConfigDO>()
                .eq(AgentConfigDO::getStatus, 2) // ACTIVE only
                .and(w -> w.eq(AgentConfigDO::getWorkspaceId, workspaceId)
                        .or().eq(AgentConfigDO::getVisibility, 3))
                .orderByAsc(AgentConfigDO::getAgentId));
    }

    public Long save(AgentConfigDO agentConfigDO) {
        agentConfigMapper.insert(agentConfigDO);
        return agentConfigDO.getAgentId();
    }

    public boolean updateById(AgentConfigDO agentConfigDO) {
        return agentConfigMapper.updateById(agentConfigDO) > 0;
    }

    public boolean removeById(Long agentId) {
        return agentConfigMapper.deleteById(agentId) > 0;
    }
}
