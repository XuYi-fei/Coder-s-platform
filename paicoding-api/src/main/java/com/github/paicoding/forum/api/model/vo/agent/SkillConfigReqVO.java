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
    private String tags;
    private Integer visibility = 3; // PUBLIC by default for builtin skills
}
