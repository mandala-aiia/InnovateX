package com.alec.InnovateX.spring.mvc;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MVC 校验集成演示（Spring 自带 @Validated + @InitBinder 注册的自定义 Validator，不依赖 Bean Validation）：
 * - /api/validate：处理器自带 BindingResult 参数——校验失败不抛异常，自己决定返回什么
 * - /api/validate-strict：没有 BindingResult 参数——校验失败抛 BindException，
 *   由 @RestControllerAdvice 的 @ExceptionHandler(BindException) 统一转 400
 */
@RestController
public class ValidationController {

    /**
     * 控制器局部 @InitBinder：给本控制器每个参数的 WebDataBinder 注册校验器。
     * 注意不能依赖 binder.getTarget() 判断类型（调用时 target 可能为 null），
     * 是否真的校验交给 @Validated 注解 + Validator.supports 组合判断
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(new PersonValidator());
    }

    @PostMapping("/api/validate")
    public Map<String, Object> validate(@Validated PersonRequest request, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ":" + fe.getDefaultMessage()).toList();
            return Map.of("valid", false, "errors", errors);
        }
        return Map.of("valid", true, "name", request.getName(), "age", request.getAge());
    }

    @PostMapping("/api/validate-strict")
    public Map<String, Object> validateStrict(@Validated PersonRequest request) {
        return Map.of("valid", true, "name", request.getName());
    }
}
