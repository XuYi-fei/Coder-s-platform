package com.github.paicoding.forum.service.agent.tool;

import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, ToolExecutor> executors;

    public ToolRegistry(List<ToolExecutor> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(ToolExecutor::supportedType, Function.identity()));
        log.info("ToolRegistry initialized with executors: {}", executors.keySet());
    }

    public ToolExecutionResult execute(ToolConfigDO tool, String inputJson) {
        ToolExecutor executor = executors.get(tool.getToolType());
        if (executor == null) {
            log.warn("No executor found for tool type: {}", tool.getToolType());
            return ToolExecutionResult.failure("Unsupported tool type: " + tool.getToolType());
        }
        log.info("Executing tool '{}' (type={}) with input: {}", tool.getName(), tool.getToolType(), inputJson);
        return executor.execute(tool, inputJson);
    }

    public boolean supports(String toolType) {
        return executors.containsKey(toolType);
    }
}
