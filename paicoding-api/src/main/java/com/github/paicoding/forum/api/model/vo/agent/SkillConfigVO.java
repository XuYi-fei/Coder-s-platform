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
    /** PROMPT_TEMPLATE / RAG_QUERY / CODE_SNIPPET / HTTP_PIPELINE */
    private String skillType;
    private String content;
    private String tags;
    private Integer visibility;
    private Integer status;
    private Integer isSystem;
    private LocalDateTime createdAt;
}
