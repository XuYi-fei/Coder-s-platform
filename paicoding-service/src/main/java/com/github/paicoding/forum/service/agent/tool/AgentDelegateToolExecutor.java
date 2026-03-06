package com.github.paicoding.forum.service.agent.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentDelegateToolExecutor implements ToolExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public String supportedType() {
        return "AGENT_DELEGATE";
    }

    @Override
    public ToolExecutionResult execute(ToolConfigDO tool, String inputJson) {
        // Agent delegation is handled at the engine level, not here.
        // This executor is a fallback placeholder.
        log.info("AgentDelegate tool '{}' delegating with input: {}", tool.getName(), inputJson);
        return ToolExecutionResult.success(
                "{\"delegated\": true, \"message\": \"Task delegated to sub-agent\"}",
                0L
        );
    }
}
