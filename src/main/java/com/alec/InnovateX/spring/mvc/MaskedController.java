package com.alec.InnovateX.spring.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 注意这里是普通 @Controller 而非 @RestController：
 * @RestController 的所有返回值都会被内置的 RequestResponseBodyMethodProcessor 接管
 * （它排在自定义处理器之前），自定义返回值处理器永远轮不到——
 * 这本身就是理解"处理器按注册顺序选择"的最佳教材
 */
@Controller
public class MaskedController {

    @MaskedResult
    @GetMapping("/api/card")
    public MaskedPayload cardNumber() {
        return new MaskedPayload("6222020001234567");
    }
}
