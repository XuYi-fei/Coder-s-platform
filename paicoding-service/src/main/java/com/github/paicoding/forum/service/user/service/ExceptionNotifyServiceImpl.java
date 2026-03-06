package com.github.paicoding.forum.service.user.service;

import com.github.paicoding.forum.core.exception.model.ExceptionContext;
import com.github.paicoding.forum.core.exception.service.ExceptionNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 异常通知服务 — 简单日志实现（邮件通知功能已裁剪）
 *
 * @author XuYifei
 * @date 2026-03-07
 */
@Slf4j
@Service
public class ExceptionNotifyServiceImpl implements ExceptionNotifyService {

    @Override
    public boolean sendExceptionNotify(ExceptionContext context, String notifyEmails) {
        log.warn("[ExceptionNotify] {} - {}: {}", context.getSeverity(),
                context.getExceptionType(), context.getExceptionMessage());
        return true;
    }

    @Override
    public boolean sendExceptionNotify(ExceptionContext context) {
        return sendExceptionNotify(context, null);
    }

    @Override
    public boolean shouldNotify(ExceptionContext context, boolean enableRateLimit) {
        return true;
    }
}
