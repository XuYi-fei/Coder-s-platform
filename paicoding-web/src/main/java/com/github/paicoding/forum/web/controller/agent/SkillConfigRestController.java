package com.github.paicoding.forum.web.controller.agent;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.agent.SkillConfigReqVO;
import com.github.paicoding.forum.api.model.vo.agent.SkillConfigVO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.agent.service.SkillConfigService;
import com.github.paicoding.forum.service.agent.service.SkillUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "技能配置管理")
@RestController
@RequestMapping("/agent/api")
@RequiredArgsConstructor
public class SkillConfigRestController {

    private final SkillConfigService skillConfigService;
    private final SkillUploadService skillUploadService;

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

    /**
     * Upload a skill as a .zip file.
     * The zip must contain a SKILL.md at its root (or one folder deep).
     * The file is stored in OSS; metadata is extracted from SKILL.md frontmatter.
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping("/skills/upload")
    public ResVo<Long> uploadSkill(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "上传的文件不能为空");
        }
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!originalName.toLowerCase().endsWith(".zip")) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "只支持上传 .zip 格式的 skill 文件");
        }
        try {
            SkillUploadService.SkillUploadResult result = skillUploadService.processUpload(file);
            SkillConfigReqVO req = result.getReqVO();
            req.setSkillMd(result.getSkillMd());
            req.setOssUrl(result.getOssUrl());
            Long userId = ReqInfoContext.getReqInfo().getUserId();
            Long skillId = skillConfigService.createSkill(req, userId);
            return ResVo.ok(skillId);
        } catch (IllegalArgumentException e) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, e.getMessage());
        } catch (Exception e) {
            log.error("Skill upload failed", e);
            return ResVo.fail(StatusEnum.UNEXPECT_ERROR, e.getMessage());
        }
    }
}
