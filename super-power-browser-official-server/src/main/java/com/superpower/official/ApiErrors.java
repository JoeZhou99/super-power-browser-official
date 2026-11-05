package com.superpower.official;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);
    @ExceptionHandler(RegistrationFailure.class)
    ResponseEntity<?> registration(RegistrationFailure error) { return response(error.status(), error.getMessage()); }
    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<?> duplicate() { return response(409, "该账号已存在，请更换账号或直接登录客户端。"); }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<?> malformed() { return response(400, "请求格式不正确，请检查填写内容后重试。"); }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<?> database(DataAccessException error) {
        LOG.error("Registration database operation failed: {}", error.getClass().getSimpleName());
        return response(503, "注册服务暂不可用，请稍后再试或联系微信客服。");
    }
    private ResponseEntity<?> response(int status, String message) { return ResponseEntity.status(status).body(Map.of("message", message)); }
}
