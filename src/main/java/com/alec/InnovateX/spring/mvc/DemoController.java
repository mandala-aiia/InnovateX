package com.alec.InnovateX.spring.mvc;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * MVC 演示控制器——每个端点对应一个演示点：
 * - GET  persons/{id}        : HandlerMapping/HandlerAdapter 基本流程 + JSON 序列化
 * - POST persons             : @RequestBody 反序列化
 * - GET  client              : 自定义 HandlerMethodArgumentResolver（参数凭空解析）
 * - GET  persons/{id} + Accept: text/csv : 内容协商走自定义 HttpMessageConverter
 * - GET  error               : 业务异常 -> @RestControllerAdvice 统一处理
 * - GET  boom                : 未被匹配的异常 -> 兜底 500
 * - GET  async               : Callable 异步处理（工作线程执行，释放容器线程）
 * - GET  sse                 : SseEmitter 服务端推送
 * - POST upload              : multipart 文件上传（MultipartResolver）
 * （@MaskedResult 脱敏端点在 MaskedController——见其类注释）
 */
@RestController
public class DemoController {

    @GetMapping("/api/persons/{id}")
    public Person getPerson(@PathVariable long id) {
        return new Person(id, "InnovateX-User-" + id);
    }

    @PostMapping("/api/persons")
    public Person createPerson(@RequestBody Person person) {
        return new Person(person.id(), "已创建:" + person.name());
    }

    /** ClientContext 参数没有任何注解——由 ClientContextArgumentResolver 从请求头解析 */
    @GetMapping("/api/client")
    public ClientContext client(ClientContext client,
                                @RequestHeader(value = "Host", required = false) String host) {
        System.out.println("[DemoController] 自定义参数已解析: " + client + "（Host=" + host + "）");
        return client;
    }

    @GetMapping("/api/error")
    public Person error() {
        throw new BusinessException(418, "BIZ_001", "业务异常示例");
    }

    @GetMapping("/api/boom")
    public Person boom() {
        throw new IllegalStateException("没有任何 @ExceptionHandler 匹配的异常");
    }

    /** 异步：返回 Callable 后容器线程立即释放，业务逻辑交给 MVC 的 TaskExecutor 执行 */
    @GetMapping("/api/async")
    public Callable<String> async() {
        return () -> {
            System.out.println("[DemoController] Callable 在工作线程执行: " + Thread.currentThread().getName());
            Thread.sleep(50);
            return "async-result@[" + Thread.currentThread().getName() + "]";
        };
    }

    /** SSE：返回 SseEmitter 后立即发送三批事件并结束（发送发生在 async 启动前会被缓冲） */
    @GetMapping(value = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() throws IOException {
        SseEmitter emitter = new SseEmitter(5000L);
        for (int i = 1; i <= 3; i++) {
            emitter.send(SseEmitter.event().name("tick").data("event-" + i));
        }
        emitter.complete();
        return emitter;
    }

    /** multipart 上传：MultipartResolver 把 multipart/form-data 解析成 MultipartFile 参数 */
    @PostMapping("/api/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Map.of(
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "content", new String(file.getBytes(), StandardCharsets.UTF_8));
    }
}
