package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

@Data
public class SkillConfigReqVO {
    private Long workspaceId;
    private String name;
    private String displayName;
    private String description;
    private String skillType;
    private String content;
    /** Raw SKILL.md content (populated by upload service for FILE_BASED skills) */
    private String skillMd;
    /** OSS storage path of the zip (populated by upload service) */
    private String ossPath;
    /** Public URL of the skill zip (populated by upload service) */
    private String ossUrl;
    private String tags;
    private Integer visibility = 3; // PUBLIC by default for builtin skills
}
