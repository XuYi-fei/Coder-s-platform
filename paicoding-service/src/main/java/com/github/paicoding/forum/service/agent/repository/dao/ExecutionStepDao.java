package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.ExecutionStepDO;
import com.github.paicoding.forum.service.agent.repository.mapper.ExecutionStepMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执行步骤 DAO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Repository
@RequiredArgsConstructor
public class ExecutionStepDao {

    private final ExecutionStepMapper executionStepMapper;

    public List<ExecutionStepDO> listBySession(Long sessionId) {
        return executionStepMapper.selectList(new LambdaQueryWrapper<ExecutionStepDO>()
                .eq(ExecutionStepDO::getSessionId, sessionId)
                .orderByAsc(ExecutionStepDO::getStepOrder));
    }

    public Long save(ExecutionStepDO stepDO) {
        executionStepMapper.insert(stepDO);
        return stepDO.getStepId();
    }

    public boolean updateCompleted(Long stepId, String outputResult, int tokensUsed, int durationMs) {
        return executionStepMapper.update(null, new LambdaUpdateWrapper<ExecutionStepDO>()
                .eq(ExecutionStepDO::getStepId, stepId)
                .set(ExecutionStepDO::getStatus, "COMPLETED")
                .set(ExecutionStepDO::getOutputResult, outputResult)
                .set(ExecutionStepDO::getTokensUsed, tokensUsed)
                .set(ExecutionStepDO::getDurationMs, durationMs)
                .set(ExecutionStepDO::getCompletedAt, LocalDateTime.now())) > 0;
    }

    public boolean updateFailed(Long stepId, String errorMessage) {
        return executionStepMapper.update(null, new LambdaUpdateWrapper<ExecutionStepDO>()
                .eq(ExecutionStepDO::getStepId, stepId)
                .set(ExecutionStepDO::getStatus, "FAILED")
                .set(ExecutionStepDO::getErrorMessage, errorMessage)
                .set(ExecutionStepDO::getCompletedAt, LocalDateTime.now())) > 0;
    }
}
