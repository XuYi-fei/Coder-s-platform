package com.github.paicoding.forum.service.agent.service;

import com.github.paicoding.forum.api.model.vo.agent.ModelConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.ModelConfigVO;
import com.github.paicoding.forum.service.agent.repository.dao.ModelConfigDao;
import com.github.paicoding.forum.service.agent.repository.entity.ModelConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigService {

    private final ModelConfigDao modelConfigDao;

    public List<ModelConfigVO> listModels(Long workspaceId) {
        return modelConfigDao.listAvailableForWorkspace(workspaceId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public ModelConfigVO getModel(Long modelConfigId) {
        ModelConfigDO model = modelConfigDao.getById(modelConfigId);
        return model != null ? toVO(model) : null;
    }

    public Long createModel(ModelConfigReqVO req, Long creatorUserId) {
        ModelConfigDO model = new ModelConfigDO();
        model.setWorkspaceId(req.getWorkspaceId());
        model.setName(req.getName());
        model.setProvider(req.getProvider());
        model.setModelName(req.getModelName());
        model.setBaseUrl(req.getBaseUrl());
        model.setApiKey(req.getApiKey()); // TODO: AES encrypt
        model.setMaxTokens(req.getMaxTokens());
        model.setTemperature(req.getTemperature());
        model.setVisibility(req.getVisibility());
        model.setStatus(1);
        model.setCreatorUserId(creatorUserId);
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        return modelConfigDao.save(model);
    }

    public boolean updateModel(Long modelConfigId, ModelConfigReqVO req) {
        ModelConfigDO model = modelConfigDao.getById(modelConfigId);
        if (model == null) return false;
        if (req.getName() != null) model.setName(req.getName());
        if (req.getModelName() != null) model.setModelName(req.getModelName());
        if (req.getBaseUrl() != null) model.setBaseUrl(req.getBaseUrl());
        if (req.getApiKey() != null && !req.getApiKey().isBlank()) model.setApiKey(req.getApiKey());
        if (req.getMaxTokens() != null) model.setMaxTokens(req.getMaxTokens());
        if (req.getTemperature() != null) model.setTemperature(req.getTemperature());
        if (req.getVisibility() != null) model.setVisibility(req.getVisibility());
        model.setUpdatedAt(LocalDateTime.now());
        return modelConfigDao.updateById(model);
    }

    public boolean deleteModel(Long modelConfigId) {
        return modelConfigDao.removeById(modelConfigId);
    }

    /**
     * 根据 DB 中的模型配置，动态创建 ChatClient
     */
    public ChatClient buildChatClient(Long modelConfigId) {
        ModelConfigDO model = modelConfigDao.getById(modelConfigId);
        if (model == null) {
            throw new IllegalArgumentException("Model config not found: " + modelConfigId);
        }
        if (model.getStatus() != 1) {
            throw new IllegalStateException("Model is disabled: " + model.getName());
        }
        return buildChatClientFromConfig(model);
    }

    public ChatClient buildChatClientFromConfig(ModelConfigDO model) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(model.getApiKey())
                .baseUrl(model.getBaseUrl())
                .build();

        double temperature = model.getTemperature() != null
                ? model.getTemperature().doubleValue()
                : 0.7;

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model.getModelName())
                        .maxTokens(model.getMaxTokens())
                        .temperature(temperature)
                        .build())
                .build();

        return ChatClient.builder(chatModel).build();
    }

    private ModelConfigVO toVO(ModelConfigDO model) {
        ModelConfigVO vo = new ModelConfigVO();
        vo.setModelConfigId(model.getModelConfigId());
        vo.setWorkspaceId(model.getWorkspaceId());
        vo.setName(model.getName());
        vo.setProvider(model.getProvider());
        vo.setModelName(model.getModelName());
        vo.setBaseUrl(model.getBaseUrl());
        // Mask API key: show first 6 + **** + last 4
        String key = model.getApiKey();
        if (key != null && key.length() > 10) {
            vo.setApiKeyMasked(key.substring(0, 6) + "****" + key.substring(key.length() - 4));
        } else {
            vo.setApiKeyMasked("****");
        }
        vo.setMaxTokens(model.getMaxTokens());
        vo.setTemperature(model.getTemperature());
        vo.setVisibility(model.getVisibility());
        vo.setStatus(model.getStatus());
        vo.setCreatedAt(model.getCreatedAt());
        return vo;
    }
}
