package com.alec.InnovateX.spring.resource;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * 数据校验器：配合 DataBinder 实现"先绑定、后校验"一条龙。
 * <p>
 * Spring Validator 契约只有两步：
 * <ul>
 *   <li>supports：声明本校验器能处理的目标类型；</li>
 *   <li>validate：把违规写进 Errors（字段级 rejectValue / 对象级 reject），
 *       DataBinder 的 BindingResult 随后统一收集。</li>
 * </ul>
 * 校验规则：host 必填；port 必须在 1~65535 之间。
 */
public class ServerNodeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ServerNode.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // 工具类：目标为空/白串时登记一条字段错误
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "host", "host.required", "主机名不能为空");
        ServerNode node = (ServerNode) target;
        if (node.getPort() != null && (node.getPort() < 1 || node.getPort() > 65535)) {
            errors.rejectValue("port", "port.outOfRange", "端口必须在 1~65535 之间");
        }
    }
}
