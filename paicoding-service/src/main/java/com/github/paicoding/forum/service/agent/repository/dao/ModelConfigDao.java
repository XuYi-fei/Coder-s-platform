package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.ModelConfigDO;
import com.github.paicoding.forum.service.agent.repository.mapper.ModelConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型配置 DAO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Repository
@RequiredArgsConstructor
public class ModelConfigDao {

    private final ModelConfigMapper modelConfigMapper;

    public ModelConfigDO getById(Long modelConfigId) {
        return modelConfigMapper.selectById(modelConfigId);
    }

    public List<ModelConfigDO> listByWorkspace(Long workspaceId) {
        return modelConfigMapper.selectList(new LambdaQueryWrapper<ModelConfigDO>()
                .eq(ModelConfigDO::getWorkspaceId, workspaceId)
                .eq(ModelConfigDO::getStatus, 1)
                .orderByAsc(ModelConfigDO::getModelConfigId));
    }

    /**
     * 查询工作区可用模型（含全局公开模型）
     */
    public List<ModelConfigDO> listAvailableForWorkspace(Long workspaceId) {
        return modelConfigMapper.selectList(new LambdaQueryWrapper<ModelConfigDO>()
                .eq(ModelConfigDO::getStatus, 1)
                .and(w -> w.isNull(ModelConfigDO::getWorkspaceId)
                        .or().eq(ModelConfigDO::getWorkspaceId, workspaceId)
                        .or().eq(ModelConfigDO::getVisibility, 3))
                .orderByAsc(ModelConfigDO::getModelConfigId));
    }

    public Long save(ModelConfigDO modelConfigDO) {
        modelConfigMapper.insert(modelConfigDO);
        return modelConfigDO.getModelConfigId();
    }

    public boolean updateById(ModelConfigDO modelConfigDO) {
        return modelConfigMapper.updateById(modelConfigDO) > 0;
    }

    public boolean removeById(Long modelConfigId) {
        return modelConfigMapper.deleteById(modelConfigId) > 0;
    }
}
