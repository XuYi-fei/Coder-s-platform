package com.github.paicoding.forum.web.global;

import com.github.paicoding.forum.api.model.exception.ForumAdviceException;
import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.constants.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 全局异常处理器
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ForumAdviceException.class)
    public ResVo<String> handleForumAdviceException(ForumAdviceException e) {
        return ResVo.fail(e.getStatus());
    }

    @ExceptionHandler(value = {SQLException.class, DataAccessException.class})
    public ResVo<String> handleDatabaseException(Exception e) {
        log.error("数据库异常", e);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }

    @ExceptionHandler(value = IOException.class)
    public ResVo<String> handleIOException(IOException e) {
        log.error("IO异常", e);
        return ResVo.fail(StatusEnum.UPLOAD_PIC_FAILED);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResVo<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResVo<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
    }

    @ExceptionHandler(value = Exception.class)
    public ResVo<String> handleException(Exception e) {
        log.error("系统异常", e);
        return ResVo.fail(StatusEnum.UNEXPECT_ERROR);
    }
}
