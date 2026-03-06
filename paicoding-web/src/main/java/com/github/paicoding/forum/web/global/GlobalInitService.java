package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.util.NumUtil;
import com.github.paicoding.forum.core.util.SessionUtil;
import com.github.paicoding.forum.service.user.service.LoginService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.global.vo.GlobalVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class GlobalInitService {

    @Value("${env.name}")
    private String env;

    @Autowired
    private UserService userService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    /**
     * 全局属性配置
     */
    public GlobalVo globalAttr() {
        GlobalVo vo = new GlobalVo();
        vo.setEnv(env);
        vo.setSiteInfo(globalViewConfig);

        try {
            if (ReqInfoContext.getReqInfo() != null && NumUtil.upZero(ReqInfoContext.getReqInfo().getUserId())) {
                vo.setIsLogin(true);
                vo.setUser(ReqInfoContext.getReqInfo().getUser());
                vo.setMsgNum(ReqInfoContext.getReqInfo().getMsgNum());
            } else {
                vo.setIsLogin(false);
            }
        } catch (Exception e) {
            log.error("loginCheckError:", e);
        }
        return vo;
    }

    /**
     * 初始化用户信息
     */
    public void initLoginUser(ReqInfoContext.ReqInfo reqInfo) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (request.getCookies() == null) {
            Optional.ofNullable(request.getHeader("Authorization"))
                    .ifPresent(token -> initLoginUser(token, reqInfo));
            return;
        }
        Optional.ofNullable(SessionUtil.findCookieByName(request, LoginService.SESSION_KEY))
                .ifPresent(cookie -> initLoginUser(cookie.getValue(), reqInfo));
    }

    public void initLoginUser(String session, ReqInfoContext.ReqInfo reqInfo) {
        BaseUserInfoDTO user = userService.getAndUpdateUserIpInfoBySessionId(session, null);
        reqInfo.setSession(session);
        if (user != null) {
            reqInfo.setUserId(user.getUserId());
            reqInfo.setUser(user);
        }
    }
}
