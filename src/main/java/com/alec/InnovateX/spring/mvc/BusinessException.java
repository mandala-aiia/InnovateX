package com.alec.InnovateX.spring.mvc;

/** 业务异常：携带 HTTP 状态码与业务错误码，由 @RestControllerAdvice 统一转成响应 */
public class BusinessException extends RuntimeException {

    private final int httpStatus;

    private final String code;

    public BusinessException(int httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }
}
