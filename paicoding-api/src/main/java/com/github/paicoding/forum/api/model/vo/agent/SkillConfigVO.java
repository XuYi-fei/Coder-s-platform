package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SkillConfigVO {
    private Long skillId;
    private Long workspaceId;
    private String name;
    private String displayName;
    private String description;
    /** PROMPT_TEMPLATE / CODE_SNIPPET / HTTP_PIPELINE / FILE_BASED */
    private String skillType;
    private String content;
    /** Raw SKILL.md content (FILE_BASED type) */
    private String skillMd;
    /** Public URL of the uploaded skill zip */
    private String ossUrl;
    private String tags;
    private Integer visibility;
    private Integer status;
    private Integer isSystem;
    private LocalDateTime createdAt;
}
