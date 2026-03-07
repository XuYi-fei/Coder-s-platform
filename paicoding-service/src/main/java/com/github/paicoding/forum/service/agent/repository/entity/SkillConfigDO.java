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
    /**
     * Skill 类型：
     * SIMPLE — 仅含 SKILL.md，无额外资源；可在线编辑或上传只含 SKILL.md 的 zip
     * FOLDER — 含 SKILL.md + 额外资源目录（scripts/、references/、assets/）；需上传 zip
     */
    private String skillType;
    /** SKILL.md 正文（frontmatter 之后的 Markdown 指令），冗余存储方便快速读取 */
    private String content;
    /** 完整的 SKILL.md 内容（YAML frontmatter + Markdown 正文） */
    private String skillMd;
    /** FOLDER 类型：zip 在 OSS 中的存储路径 */
    private String ossPath;
    /** FOLDER 类型：zip 的可访问公开 URL */
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
