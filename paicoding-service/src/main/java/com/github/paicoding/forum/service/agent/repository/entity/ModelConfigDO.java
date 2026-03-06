package com.github.paicoding.forum.service.agent.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型配置实体（对应 model_config 表）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
@TableName("model_config")
public class ModelConfigDO {
    @TableId(value = "model_config_id", type = IdType.AUTO)
    private Long modelConfigId;

    private Long workspaceId;
    private String name;
    private String provider;
    private String modelName;
    private String baseUrl;
    /** AES 加密存储的 API Key */
    private String apiKey;
    private Integer maxTokens;
    private BigDecimal temperature;
    private String extraParams;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED */
    private Integer visibility;
    /** 1=正常 2=禁用 */
    private Integer status;
    private Long creatorUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
