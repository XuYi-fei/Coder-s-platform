package com.github.paicoding.forum.service.agent.tool;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolExecutionResult {
    private boolean success;
    private String output;  // JSON string or plain text result
    private String errorMessage;
    private int tokensUsed;
    private long durationMs;

    public static ToolExecutionResult success(String output, long durationMs) {
        return ToolExecutionResult.builder()
                .success(true)
                .output(output)
                .durationMs(durationMs)
                .build();
    }

    public static ToolExecutionResult failure(String errorMessage) {
        return ToolExecutionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
