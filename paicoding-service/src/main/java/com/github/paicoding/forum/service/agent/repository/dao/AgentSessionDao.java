package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.AgentSessionDO;
import com.github.paicoding.forum.service.agent.repository.mapper.AgentSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 执行会话 DAO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Repository
@RequiredArgsConstructor
public class AgentSessionDao {

    private final AgentSessionMapper agentSessionMapper;

    public AgentSessionDO getById(Long sessionId) {
        return agentSessionMapper.selectById(sessionId);
    }

    public List<AgentSessionDO> listByUser(Long userId, Long agentId) {
        LambdaQueryWrapper<AgentSessionDO> wrapper = new LambdaQueryWrapper<AgentSessionDO>()
                .eq(AgentSessionDO::getUserId, userId)
                .orderByDesc(AgentSessionDO::getStartedAt);
        if (agentId != null) {
            wrapper.eq(AgentSessionDO::getEntryAgentId, agentId);
        }
        return agentSessionMapper.selectList(wrapper);
    }

    public Long save(AgentSessionDO sessionDO) {
        agentSessionMapper.insert(sessionDO);
        return sessionDO.getSessionId();
    }

    public boolean updateStatus(Long sessionId, String status, String finalOutput) {
        return agentSessionMapper.update(null, new LambdaUpdateWrapper<AgentSessionDO>()
                .eq(AgentSessionDO::getSessionId, sessionId)
                .set(AgentSessionDO::getStatus, status)
                .set(finalOutput != null, AgentSessionDO::getFinalOutput, finalOutput)
                .set(AgentSessionDO::getCompletedAt, LocalDateTime.now())) > 0;
    }

    public boolean updateTotalTokens(Long sessionId, int tokensTotal) {
        return agentSessionMapper.update(null, new LambdaUpdateWrapper<AgentSessionDO>()
                .eq(AgentSessionDO::getSessionId, sessionId)
                .set(AgentSessionDO::getTokensTotal, tokensTotal)) > 0;
    }
}
