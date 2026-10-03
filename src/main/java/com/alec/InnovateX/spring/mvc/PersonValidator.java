package com.alec.InnovateX.spring.mvc;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * MVC 校验用的自定义 Validator：不依赖 Bean Validation（hibernate-validator），
 * 通过 @InitBinder 注册到 WebDataBinder 后，Spring 自带的 @Validated 注解就会触发它
 */
public class PersonValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // 防御：@InitBinder 注册的校验器可能被应用到任意参数 binder 上
        if (!supports(target.getClass())) {
            return;
        }
        ValidationUtils.rejectIfEmpty(errors, "name", "name.empty", "姓名不能为空");
        PersonRequest request = (PersonRequest) target;
        if (request.getAge() != null && request.getAge() < 0) {
            errors.rejectValue("age", "age.negative", "年龄不能为负数");
        }
    }
}
