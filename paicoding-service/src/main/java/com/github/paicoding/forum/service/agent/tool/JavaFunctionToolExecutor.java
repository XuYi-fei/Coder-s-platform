package com.github.paicoding.forum.service.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.service.agent.repository.entity.ToolConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaFunctionToolExecutor implements ToolExecutor {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    @Override
    public String supportedType() {
        return "JAVA_FUNCTION";
    }

    @Override
    public ToolExecutionResult execute(ToolConfigDO tool, String inputJson) {
        long start = System.currentTimeMillis();
        try {
            JsonNode config = objectMapper.readTree(tool.getConfig());
            String beanName = config.path("bean_name").asText();
            String methodName = config.path("method_name").asText();

            Object bean = applicationContext.getBean(beanName);
            Method method = null;
            for (Method m : bean.getClass().getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == String.class) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                return ToolExecutionResult.failure("Method not found: " + beanName + "." + methodName + "(String)");
            }
            Object result = method.invoke(bean, inputJson);
            String output = result != null ? result.toString() : "null";
            return ToolExecutionResult.success(output, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("JavaFunction tool '{}' failed: {}", tool.getName(), e.getMessage());
            return ToolExecutionResult.failure("Java function call failed: " + e.getMessage());
        }
    }
}
