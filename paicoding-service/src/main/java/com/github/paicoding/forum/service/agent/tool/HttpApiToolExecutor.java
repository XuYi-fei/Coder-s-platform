package com.github.paicoding.forum.service.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpApiToolExecutor implements ToolExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public String supportedType() {
        return "HTTP_API";
    }

    @Override
    public ToolExecutionResult execute(ToolConfigDO tool, String inputJson) {
        long start = System.currentTimeMillis();
        try {
            JsonNode config = objectMapper.readTree(tool.getConfig());
            String url = config.path("url").asText();
            String method = config.path("method").asText("GET").toUpperCase();

            // Replace {param} placeholders in URL with values from inputJson
            JsonNode inputNode = objectMapper.readTree(inputJson);
            String resolvedUrl = url;
            if (inputNode != null && inputNode.isObject()) {
                for (var entry : (Iterable<java.util.Map.Entry<String, JsonNode>>) inputNode::fields) {
                    resolvedUrl = resolvedUrl.replace("{" + entry.getKey() + "}", entry.getValue().asText());
                }
            }

            RestClient restClient = RestClient.builder().build();

            String responseBody;
            if ("GET".equals(method)) {
                responseBody = restClient.get()
                        .uri(resolvedUrl)
                        .retrieve()
                        .body(String.class);
            } else {
                responseBody = restClient.post()
                        .uri(resolvedUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(inputJson)
                        .retrieve()
                        .body(String.class);
            }

            long duration = System.currentTimeMillis() - start;
            log.info("HTTP tool '{}' executed: {} {}, duration={}ms", tool.getName(), method, resolvedUrl, duration);
            return ToolExecutionResult.success(responseBody, duration);
        } catch (Exception e) {
            log.error("HTTP tool '{}' failed: {}", tool.getName(), e.getMessage());
            return ToolExecutionResult.failure("HTTP request failed: " + e.getMessage());
        }
    }
}
