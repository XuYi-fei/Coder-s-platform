package com.github.paicoding.forum.service.agent.tool;

import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;

/**
 * 工具执行器接口（可插拔设计）
 * 每种 tool_type 对应一个实现类
 */
public interface ToolExecutor {
    /**
     * @return 支持的工具类型，如 "HTTP_API"
     */
    String supportedType();

    /**
     * 执行工具
     * @param tool 工具配置
     * @param inputJson 入参（JSON 字符串）
     * @return 执行结果
     */
    ToolExecutionResult execute(ToolConfigDO tool, String inputJson);
}
