package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("skill_config")
public class SkillConfigDO {
    @TableId(value = "skill_id", type = IdType.AUTO)
    private Long skillId;
    private Long workspaceId;
    private String name;
    private String displayName;
    private String description;
    private String skillType;
    private String content;
    /** FILE_BASED: raw SKILL.md text */
    private String skillMd;
    /** FILE_BASED: OSS storage path of the zip */
    private String ossPath;
    /** FILE_BASED: public URL of the zip */
    private String ossUrl;
    private String tags;
    private Integer visibility;
    private Integer status;
    private Integer isSystem;
    private Integer version;
    private Long creatorUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
