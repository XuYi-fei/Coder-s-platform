package com.github.paicoding.forum.service.agent.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.service.agent.repository.dao.ExecutionStepDao;
import com.github.paicoding.forum.service.agent.repository.entity.AgentConfigDO;
import com.github.paicoding.forum.service.agent.repository.entity.ExecutionStepDO;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import com.github.paicoding.forum.service.agent.tool.ToolExecutionResult;
import com.github.paicoding.forum.service.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReActAgentEngine {

    private final ToolRegistry toolRegistry;
    private final ExecutionStepDao executionStepDao;
    private final ObjectMapper objectMapper;

    /**
     * 以 ReAct 模式运行 Agent，返回 SSE 事件流
     *
     * @param chatClient  Spring AI ChatClient（已配置对应模型）
     * @param agentConfig Agent 配置
     * @param tools       Agent 绑定的工具列表
     * @param sessionId   执行会话 ID
     * @param userMessage 用户输入
     * @return 事件流（每个元素是 JSON 字符串）
     */
    public Flux<String> run(ChatClient chatClient, AgentConfigDO agentConfig,
                            List<ToolConfigDO> tools, Long sessionId, String userMessage) {
        return Flux.create(sink -> {
            try {
                executeReAct(chatClient, agentConfig, tools, sessionId, userMessage, sink);
            } catch (Exception e) {
                log.error("ReAct engine error for session {}: {}", sessionId, e.getMessage(), e);
                sink.next(toJson(AgentStreamEvent.error(e.getMessage())));
                sink.complete();
            }
        });
    }

    private void executeReAct(ChatClient chatClient, AgentConfigDO agentConfig,
                               List<ToolConfigDO> tools, Long sessionId,
                               String userMessage, FluxSink<String> sink) {
        int maxSteps = agentConfig.getMaxSteps() != null ? agentConfig.getMaxSteps() : 10;
        List<Message> messages = new ArrayList<>();

        // Build system prompt with tool descriptions
        String systemPrompt = buildSystemPrompt(agentConfig.getSystemPrompt(), tools);
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userMessage));

        // Build Spring AI ToolCallbacks for each tool
        List<ToolCallback> toolCallbacks = buildToolCallbacks(tools);

        int stepOrder = 0;
        String finalAnswer = null;

        for (int iteration = 0; iteration < maxSteps; iteration++) {
            stepOrder++;
            long iterStart = System.currentTimeMillis();

            // Call LLM
            ChatResponse response;
            try {
                var requestSpec = chatClient.prompt().messages(messages);
                if (!toolCallbacks.isEmpty()) {
                    requestSpec = requestSpec.toolCallbacks(toolCallbacks);
                }
                response = requestSpec.call().chatResponse();
            } catch (Exception e) {
                log.error("LLM call failed at iteration {}: {}", iteration, e.getMessage(), e);
                sink.next(toJson(AgentStreamEvent.error("LLM call failed: " + e.getMessage())));
                break;
            }

            if (response == null || response.getResult() == null) {
                sink.next(toJson(AgentStreamEvent.error("Empty LLM response")));
                break;
            }

            AssistantMessage assistantMsg = response.getResult().getOutput();

            // Check if there are tool calls
            if (assistantMsg.hasToolCalls()) {
                // Emit THOUGHT (the assistant reasoning before calling tool)
                String assistantText = assistantMsg.getText();
                if (assistantText != null && !assistantText.isBlank()) {
                    AgentStreamEvent thoughtEvent = AgentStreamEvent.thought(stepOrder, assistantText);
                    sink.next(toJson(thoughtEvent));
                    saveStep(sessionId, stepOrder, ReActStepType.THOUGHT, assistantText, null, null,
                            0, (int) (System.currentTimeMillis() - iterStart));
                }

                // Process each tool call
                List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
                for (AssistantMessage.ToolCall toolCall : assistantMsg.getToolCalls()) {
                    stepOrder++;
                    long toolStart = System.currentTimeMillis();
                    String toolName = toolCall.name();
                    String toolArgs = toolCall.arguments();

                    // Emit ACTION event
                    AgentStreamEvent actionEvent = AgentStreamEvent.action(stepOrder, toolName, toolArgs);
                    sink.next(toJson(actionEvent));
                    saveStep(sessionId, stepOrder, ReActStepType.ACTION, null, toolName, toolArgs,
                            0, 0);

                    // Find tool config and execute
                    ToolConfigDO toolConfig = tools.stream()
                            .filter(t -> t.getName().equals(toolName))
                            .findFirst().orElse(null);

                    ToolExecutionResult result;
                    if (toolConfig == null) {
                        result = ToolExecutionResult.failure("Tool not found: " + toolName);
                    } else {
                        result = toolRegistry.execute(toolConfig, toolArgs);
                    }

                    int toolDuration = (int) (System.currentTimeMillis() - toolStart);
                    String observationContent = result.isSuccess()
                            ? result.getOutput()
                            : "Error: " + result.getErrorMessage();

                    // Emit OBSERVATION event
                    stepOrder++;
                    AgentStreamEvent obsEvent = AgentStreamEvent.observation(stepOrder, observationContent);
                    sink.next(toJson(obsEvent));
                    saveStep(sessionId, stepOrder, ReActStepType.OBSERVATION, observationContent, null, null,
                            0, toolDuration);

                    toolResponses.add(new ToolResponseMessage.ToolResponse(
                            toolCall.id(), toolName, observationContent));
                }

                // Add assistant message and tool responses to history
                messages.add(assistantMsg);
                messages.add(new ToolResponseMessage(toolResponses));

            } else {
                // No tool calls → this is the final answer
                finalAnswer = assistantMsg.getText();
                if (finalAnswer == null || finalAnswer.isBlank()) {
                    finalAnswer = "(No response generated)";
                }

                AgentStreamEvent answerEvent = AgentStreamEvent.answer(stepOrder, finalAnswer);
                sink.next(toJson(answerEvent));
                saveStep(sessionId, stepOrder, ReActStepType.ANSWER, finalAnswer, null, null,
                        0, (int) (System.currentTimeMillis() - iterStart));
                break;
            }
        }

        if (finalAnswer == null) {
            // Max iterations reached without final answer
            String timeoutMsg = "Agent reached maximum steps (" + maxSteps + ") without a final answer.";
            sink.next(toJson(AgentStreamEvent.answer(stepOrder + 1, timeoutMsg)));
        }

        sink.next(toJson(AgentStreamEvent.done(sessionId)));
        sink.complete();
    }

    private String buildSystemPrompt(String baseSystemPrompt, List<ToolConfigDO> tools) {
        if (tools == null || tools.isEmpty()) {
            return baseSystemPrompt != null ? baseSystemPrompt : "";
        }
        StringBuilder sb = new StringBuilder(baseSystemPrompt != null ? baseSystemPrompt : "");
        sb.append("\n\n## Available Tools\n");
        sb.append("You have access to the following tools. Use them when needed to complete the task.\n\n");
        for (ToolConfigDO tool : tools) {
            sb.append("### ").append(tool.getName()).append("\n");
            sb.append("**Description**: ").append(tool.getDescription()).append("\n");
            if (tool.getInputSchema() != null) {
                sb.append("**Input Schema**: `").append(tool.getInputSchema()).append("`\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Build Spring AI ToolCallbacks backed by our ToolRegistry.
     * Uses FunctionToolCallback.builder(name, function) — the correct API in Spring AI 1.1.0-M1.
     */
    private List<ToolCallback> buildToolCallbacks(List<ToolConfigDO> tools) {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (ToolConfigDO tool : tools) {
            String inputSchema = tool.getInputSchema() != null ? tool.getInputSchema()
                    : "{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\"}}}";

            // Capture a final reference for the lambda
            final ToolConfigDO capturedTool = tool;

            ToolCallback callback = FunctionToolCallback
                    .builder(tool.getName(), (String args) -> {
                        ToolExecutionResult result = toolRegistry.execute(capturedTool, args);
                        return result.isSuccess() ? result.getOutput() : "Error: " + result.getErrorMessage();
                    })
                    .description(tool.getDescription())
                    .inputSchema(inputSchema)
                    .inputType(String.class)
                    .build();

            callbacks.add(callback);
        }
        return callbacks;
    }

    private void saveStep(Long sessionId, int stepOrder, String stepType, String content,
                          String toolName, String inputParams, int tokensUsed, int durationMs) {
        try {
            ExecutionStepDO step = new ExecutionStepDO();
            step.setSessionId(sessionId);
            step.setPlanId(0L); // ReAct mode doesn't use formal plans
            step.setStepOrder(stepOrder);
            step.setStepDesc(stepType + (toolName != null ? ": " + toolName : ""));
            step.setInputParams(inputParams);
            step.setOutputResult(content != null
                    ? "{\"content\":\"" + escapeJson(content) + "\"}"
                    : null);
            step.setStatus("COMPLETED");
            step.setTokensUsed(tokensUsed);
            step.setDurationMs(durationMs);
            step.setStartedAt(LocalDateTime.now());
            step.setCompletedAt(LocalDateTime.now());
            executionStepDao.save(step);
        } catch (Exception e) {
            log.warn("Failed to save execution step: {}", e.getMessage());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String toJson(AgentStreamEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "{\"type\":\"ERROR\",\"content\":\"serialization error\"}";
        }
    }
}
