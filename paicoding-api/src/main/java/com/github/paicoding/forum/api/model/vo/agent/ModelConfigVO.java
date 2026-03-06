package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型配置 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class ModelConfigVO {
    private Long modelConfigId;
    private Long workspaceId;
    private String name;
    private String provider;
    private String modelName;
    private String baseUrl;
    /** API Key 脱敏后展示，如 sk-****abcd */
    private String apiKeyMasked;
    private Integer maxTokens;
    private BigDecimal temperature;
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility;
    /** 1=正常 2=禁用 */
    private Integer status;
    private LocalDateTime createdAt;
}
