package com.github.paicoding.forum.web.controller.user.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import com.github.paicoding.forum.api.model.vo.user.UserInfoSaveReq;
import com.github.paicoding.forum.api.model.vo.user.UserRelationReq;
import com.github.paicoding.forum.api.model.vo.user.dto.FollowUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.user.dto.UserStatisticInfoDTO;
import com.github.paicoding.forum.core.permission.Permission;
import com.github.paicoding.forum.core.permission.UserRole;
import com.github.paicoding.forum.service.user.cahce.UserInfoCacheManager;
import com.github.paicoding.forum.service.user.service.relation.UserRelationServiceImpl;
import com.github.paicoding.forum.service.user.service.user.UserServiceImpl;
import com.github.paicoding.forum.web.controller.user.vo.UserHomeInfoVo;
import com.github.paicoding.forum.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author XuYifei
 * @date 2024/7/8
 */
@RestController
@RequestMapping(path = "user/api")
public class UserRestController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private UserRelationServiceImpl userRelationService;

    @Resource
    private UserInfoCacheManager userInfoCacheManager;

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "saveUserRelation")
    public ResVo<Boolean> saveUserRelation(@RequestBody UserRelationReq req) {
        userRelationService.saveUserRelation(req);
        return ResVo.ok(true);
    }

    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "saveUserInfo")
    @Transactional(rollbackFor = Exception.class)
    public ResVo<Boolean> saveUserInfo(@RequestBody UserInfoSaveReq req) {
        if (req.getUserId() == null || !Objects.equals(req.getUserId(), ReqInfoContext.getReqInfo().getUserId())) {
            return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "无权修改");
        }
        userInfoCacheManager.delUserInfo(req.getUserId());
        userService.saveUserInfo(req);
        return ResVo.ok(true);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "home")
    public ResultVo<UserHomeInfoVo> getUserHome(@RequestParam(name = "userId") Long userId) {
        UserHomeInfoVo vo = new UserHomeInfoVo();
        UserStatisticInfoDTO userInfo = userService.queryUserInfoWithStatistic(userId);
        vo.setUserHome(userInfo);
        return ResultVo.ok(vo);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "follows")
    public ResultVo<IPage<FollowUserInfoDTO>> getUserFollowed(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                              @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(userRelationService.getUserFollowListPagination(userId, currentPage, pageSize));
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "fans")
    public ResultVo<IPage<FollowUserInfoDTO>> getUserFans(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                              @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(userRelationService.getUserFansListPagination(userId, currentPage, pageSize));
    }
}
