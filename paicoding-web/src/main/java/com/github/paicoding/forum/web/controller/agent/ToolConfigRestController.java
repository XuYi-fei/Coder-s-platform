package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.ToolConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.ToolConfigVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.ToolConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工具配置管理 REST 控制器
 *
 * @author XuYifei
 * @date 2026-03-06
 */
@Tag(name = "工具配置管理", description = "Tool 的增删改查")
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class ToolConfigRestController {

    private final ToolConfigService toolConfigService;

    @Operation(summary = "获取工具列表")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/tools")
    public ResVo<List<ToolConfigVO>> listTools(
            @RequestParam(defaultValue = "0") Long workspaceId) {
        return ResVo.ok(toolConfigService.listTools(workspaceId));
    }

    @Operation(summary = "获取工具详情")
    @Permission(role = UserRole.LOGIN)
    @GetMapping("/tools/{toolId}")
    public ResVo<ToolConfigVO> getTool(@PathVariable Long toolId) {
        return ResVo.ok(toolConfigService.getTool(toolId));
    }

    @Operation(summary = "创建工具")
    @Permission(role = UserRole.LOGIN)
    @PostMapping("/tools")
    public ResVo<Long> createTool(@RequestBody ToolConfigReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(toolConfigService.createTool(req, userId));
    }

    @Operation(summary = "更新工具配置")
    @Permission(role = UserRole.LOGIN)
    @PutMapping("/tools/{toolId}")
    public ResVo<Boolean> updateTool(@PathVariable Long toolId, @RequestBody ToolConfigReqVO req) {
        return ResVo.ok(toolConfigService.updateTool(toolId, req));
    }

    @Operation(summary = "删除工具（软删除）")
    @Permission(role = UserRole.LOGIN)
    @DeleteMapping("/tools/{toolId}")
    public ResVo<Boolean> deleteTool(@PathVariable Long toolId) {
        return ResVo.ok(toolConfigService.deleteTool(toolId));
    }
}
