package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.SkillConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.SkillConfigVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.SkillConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "技能配置管理")
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class SkillConfigRestController {

    private final SkillConfigService skillConfigService;

    @Permission(role = UserRole.LOGIN)
    @GetMapping("/skills")
    public ResVo<List<SkillConfigVO>> listSkills() {
        return ResVo.ok(skillConfigService.listSkills());
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping("/skills/{skillId}")
    public ResVo<SkillConfigVO> getSkill(@PathVariable Long skillId) {
        return ResVo.ok(skillConfigService.getSkill(skillId));
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping("/skills")
    public ResVo<Long> createSkill(@RequestBody SkillConfigReqVO req) {
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        return ResVo.ok(skillConfigService.createSkill(req, userId));
    }

    @Permission(role = UserRole.LOGIN)
    @PutMapping("/skills/{skillId}")
    public ResVo<Boolean> updateSkill(@PathVariable Long skillId, @RequestBody SkillConfigReqVO req) {
        return ResVo.ok(skillConfigService.updateSkill(skillId, req));
    }

    @Permission(role = UserRole.LOGIN)
    @DeleteMapping("/skills/{skillId}")
    public ResVo<Boolean> deleteSkill(@PathVariable Long skillId) {
        return ResVo.ok(skillConfigService.deleteSkill(skillId));
    }
}
