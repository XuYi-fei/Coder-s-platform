package com.github.paicoding.forum.service.agent.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.agent.repository.entity.SkillConfigDO;
import com.github.paicoding.forum.service.agent.repository.mapper.SkillConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SkillConfigDao {

    private final SkillConfigMapper skillConfigMapper;

    public List<SkillConfigDO> listAvailable() {
        return skillConfigMapper.selectList(new LambdaQueryWrapper<SkillConfigDO>()
                .eq(SkillConfigDO::getStatus, 1)
                .orderByDesc(SkillConfigDO::getSkillId));
    }

    public SkillConfigDO getById(Long skillId) {
        return skillConfigMapper.selectById(skillId);
    }

    public Long save(SkillConfigDO skill) {
        skillConfigMapper.insert(skill);
        return skill.getSkillId();
    }

    public boolean updateById(SkillConfigDO skill) {
        return skillConfigMapper.updateById(skill) > 0;
    }

    public boolean deleteById(Long skillId) {
        SkillConfigDO skill = getById(skillId);
        if (skill == null || skill.getIsSystem() == 1) return false;
        skill.setStatus(2); // disable
        return updateById(skill);
    }
}
