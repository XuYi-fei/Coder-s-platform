package com.github.paicoding.forum.api.model.enums.agent;

import lombok.Getter;

/**
 * 执行步骤状态枚举
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Getter
public enum StepStatusEnum {
    PENDING("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    SKIPPED("已跳过"),
    HUMAN_PENDING("等待人工审核");

    private final String displayName;

    StepStatusEnum(String displayName) {
        this.displayName = displayName;
    }
}
