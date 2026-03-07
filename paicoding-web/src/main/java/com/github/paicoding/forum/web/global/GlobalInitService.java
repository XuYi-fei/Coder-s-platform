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

    /**
     * 鉴权开关：false=跳过认证（开发模式），true=正常校验登录（生产模式）
     * 配置项：security.auth-enabled
     */
    @Value("${security.auth-enabled:false}")
    private boolean authEnabled;

    @Autowired
    private UserService userService;

    @Resource
    private GlobalViewConfig globalViewConfig;

    /** 鉴权关闭时注入的默认管理员用户（userId=1 对应 seed 数据中的 admin） */
    private static final BaseUserInfoDTO MOCK_ADMIN;
    static {
        MOCK_ADMIN = new BaseUserInfoDTO();
        MOCK_ADMIN.setUserId(1L);
        MOCK_ADMIN.setUserName("admin");
        MOCK_ADMIN.setRole("ADMIN");
        MOCK_ADMIN.setPhoto("https://static.developers.pub/static/img/logo.b2ff606.jpeg");
        MOCK_ADMIN.setProfile("系统管理员");
    }

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
     * 初始化请求上下文中的用户信息。
     * <p>
     * 当 {@code security.auth-enabled=false}（开发模式）时，直接注入 MOCK_ADMIN，
     * 所有接口均视为已登录的管理员，无需 Cookie/Token。
     * 当 {@code security.auth-enabled=true}（生产模式）时，从 Cookie 或 Authorization
     * Header 中读取 session 并校验。
     */
    public void initLoginUser(ReqInfoContext.ReqInfo reqInfo) {
        if (!authEnabled) {
            // 鉴权关闭：注入 mock 管理员，所有接口视为已登录
            reqInfo.setUserId(MOCK_ADMIN.getUserId());
            reqInfo.setUser(MOCK_ADMIN);
            return;
        }

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
