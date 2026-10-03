package com.alec.InnovateX.spring.mvc;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * 全局异常处理：@RestControllerAdvice = @ControllerAdvice + @ResponseBody。
 * - @ExceptionHandler：处理器抛出的异常先经过 HandlerExceptionResolver 链，
 *   ExceptionHandlerExceptionResolver 会到所有 advice 里找匹配方法
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：转成携带业务码的 JSON 响应（状态码由异常自带） */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        System.out.println("[GlobalExceptionHandler] 捕获业务异常: " + ex.getCode() + " - " + ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(Map.of("code", ex.getCode(), "message", ex.getMessage()));
    }

    /** 校验失败（处理器没有 BindingResult 参数时抛出）：转 400 + 字段错误清单 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBind(BindException ex) {
        List<String> errors = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ":" + fe.getDefaultMessage()).toList();
        System.out.println("[GlobalExceptionHandler] 校验失败: " + errors);
        return ResponseEntity.badRequest()
                .body(Map.of("code", "VALIDATION_FAILED", "errors", errors));
    }

    /** 兜底：任何未被上面匹配的异常也拦下来（返回 500 + 摘要） */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        System.out.println("[GlobalExceptionHandler] 兜底捕获: " + ex.getClass().getSimpleName());
        return ResponseEntity.status(500)
                .body(Map.of("code", "INTERNAL", "message", ex.getClass().getSimpleName()));
    }
}
