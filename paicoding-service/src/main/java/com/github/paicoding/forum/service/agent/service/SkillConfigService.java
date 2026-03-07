package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.SkillConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.SkillConfigVO;
import com.github.paicoding.forum.service.agent.repository.dao.SkillConfigDao;
import com.github.paicoding.forum.service.agent.repository.entity.SkillConfigDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillConfigService {

    private final SkillConfigDao skillConfigDao;

    public List<SkillConfigVO> listSkills() {
        return skillConfigDao.listAvailable().stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    public SkillConfigVO getSkill(Long skillId) {
        SkillConfigDO skill = skillConfigDao.getById(skillId);
        return skill != null ? toVO(skill) : null;
    }

    public Long createSkill(SkillConfigReqVO req, Long creatorUserId) {
        SkillConfigDO skill = new SkillConfigDO();
        skill.setWorkspaceId(req.getWorkspaceId() != null ? req.getWorkspaceId() : 0L);
        skill.setName(req.getName());
        skill.setDisplayName(req.getDisplayName());
        skill.setDescription(req.getDescription());
        skill.setSkillType(req.getSkillType() != null ? req.getSkillType() : "PROMPT_TEMPLATE");
        skill.setContent(req.getContent());
        skill.setTags(req.getTags());
        skill.setVisibility(req.getVisibility() != null ? req.getVisibility() : 3);
        skill.setStatus(1);
        skill.setIsSystem(0);
        skill.setVersion(1);
        skill.setCreatorUserId(creatorUserId);
        skill.setCreatedAt(LocalDateTime.now());
        skill.setUpdatedAt(LocalDateTime.now());
        return skillConfigDao.save(skill);
    }

    public boolean updateSkill(Long skillId, SkillConfigReqVO req) {
        SkillConfigDO skill = skillConfigDao.getById(skillId);
        if (skill == null) return false;
        if (req.getName() != null) skill.setName(req.getName());
        if (req.getDisplayName() != null) skill.setDisplayName(req.getDisplayName());
        if (req.getDescription() != null) skill.setDescription(req.getDescription());
        if (req.getContent() != null) skill.setContent(req.getContent());
        if (req.getTags() != null) skill.setTags(req.getTags());
        skill.setUpdatedAt(LocalDateTime.now());
        return skillConfigDao.updateById(skill);
    }

    public boolean deleteSkill(Long skillId) {
        return skillConfigDao.deleteById(skillId);
    }

    private SkillConfigVO toVO(SkillConfigDO skill) {
        SkillConfigVO vo = new SkillConfigVO();
        vo.setSkillId(skill.getSkillId());
        vo.setWorkspaceId(skill.getWorkspaceId());
        vo.setName(skill.getName());
        vo.setDisplayName(skill.getDisplayName());
        vo.setDescription(skill.getDescription());
        vo.setSkillType(skill.getSkillType());
        vo.setContent(skill.getContent());
        vo.setTags(skill.getTags());
        vo.setVisibility(skill.getVisibility());
        vo.setStatus(skill.getStatus());
        vo.setIsSystem(skill.getIsSystem());
        vo.setCreatedAt(skill.getCreatedAt());
        return vo;
    }
}
