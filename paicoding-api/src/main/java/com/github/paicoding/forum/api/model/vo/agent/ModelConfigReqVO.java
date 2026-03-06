package com.github.paicoding.forum.api.model.vo.agent;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建/更新模型配置请求 VO
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Data
public class ModelConfigReqVO {
    private Long workspaceId;
    private String name;
    private String provider;
    private String modelName;
    private String baseUrl;
    /** 明文 API Key，保存时服务端加密 */
    private String apiKey;
    private Integer maxTokens = 4096;
    private BigDecimal temperature = new BigDecimal("0.70");
    /** 1=PRIVATE 2=WORKSPACE 3=PUBLIC */
    private Integer visibility = 1;
}
