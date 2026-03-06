package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.AgentConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.AgentConfigVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.AgentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 配置管理 REST 控制器
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Tag(name = "Agent 配置管理", description = "Agent 的增删改查")
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class AgentConfigRestController {

    private final AgentConfigService agentConfigService;

    @Operation(summary = "获取工作区下的 Agent 列表")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/agents")
    public ResVo<List<AgentConfigVO>> listAgents(
            @RequestParam(defaultValue = "0") Long workspaceId) {
        return ResVo.ok(agentConfigService.listAgents(workspaceId));
    }

    @Operation(summary = "获取 Agent 详情（含工具列表）")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/agents/{agentId}")
    public ResVo<AgentConfigVO> getAgent(@PathVariable Long agentId) {
        return ResVo.ok(agentConfigService.getAgent(agentId));
    }

    @Operation(summary = "创建 Agent")
    @Permission(role = UserRole.LOGIN)
    @PostMapping("/agents")
    public ResVo<Long> createAgent(@RequestBody AgentConfigReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(agentConfigService.createAgent(req, userId));
    }

    @Operation(summary = "更新 Agent 配置")
    @Permission(role = UserRole.LOGIN)
    @PutMapping("/agents/{agentId}")
    public ResVo<Boolean> updateAgent(@PathVariable Long agentId, @RequestBody AgentConfigReqVO req) {
        return ResVo.ok(agentConfigService.updateAgent(agentId, req));
    }

    @Operation(summary = "激活 Agent（DRAFT → ACTIVE）")
    @Permission(role = UserRole.LOGIN)
    @PostMapping("/agents/{agentId}/activate")
    public ResVo<Boolean> activateAgent(@PathVariable Long agentId) {
        return ResVo.ok(agentConfigService.activateAgent(agentId));
    }

    @Operation(summary = "删除 Agent（软删除归档）")
    @Permission(role = UserRole.LOGIN)
    @DeleteMapping("/agents/{agentId}")
    public ResVo<Boolean> deleteAgent(@PathVariable Long agentId) {
        return ResVo.ok(agentConfigService.deleteAgent(agentId));
    }
}
