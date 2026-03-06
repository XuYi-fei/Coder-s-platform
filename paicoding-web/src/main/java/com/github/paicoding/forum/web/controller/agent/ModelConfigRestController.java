package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.ModelConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.ModelConfigVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型提供商配置管理 REST 控制器
 * 支持通过数据库动态管理 LLM 模型配置（区别于 YAML 静态配置的 ChatV2）
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Tag(name = "模型提供商配置", description = "Agent 平台的模型配置管理")
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class ModelConfigRestController {

    private final ModelConfigService modelConfigService;

    @Operation(summary = "获取可用模型列表")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/models")
    public ResVo<List<ModelConfigVO>> listModels(
            @RequestParam(defaultValue = "0") Long workspaceId) {
        return ResVo.ok(modelConfigService.listModels(workspaceId));
    }

    @Operation(summary = "获取模型详情")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/models/{modelConfigId}")
    public ResVo<ModelConfigVO> getModel(@PathVariable Long modelConfigId) {
        return ResVo.ok(modelConfigService.getModel(modelConfigId));
    }

    @Operation(summary = "创建模型配置")
    @Permission(role = UserRole.LOGIN)
    @PostMapping("/models")
    public ResVo<Long> createModel(@RequestBody ModelConfigReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(modelConfigService.createModel(req, userId));
    }

    @Operation(summary = "更新模型配置（API Key 留空则不更新）")
    @Permission(role = UserRole.LOGIN)
    @PutMapping("/models/{modelConfigId}")
    public ResVo<Boolean> updateModel(@PathVariable Long modelConfigId,
                                      @RequestBody ModelConfigReqVO req) {
        return ResVo.ok(modelConfigService.updateModel(modelConfigId, req));
    }

    @Operation(summary = "删除模型配置")
    @Permission(role = UserRole.LOGIN)
    @DeleteMapping("/models/{modelConfigId}")
    public ResVo<Boolean> deleteModel(@PathVariable Long modelConfigId) {
        return ResVo.ok(modelConfigService.deleteModel(modelConfigId));
    }
}
