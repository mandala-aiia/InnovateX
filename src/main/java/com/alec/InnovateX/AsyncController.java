package com.alec.InnovateX;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AsyncController {
    private final Map<String, DeferredResult<String>> deferredResults = new ConcurrentHashMap<>();

    // 客户端发起长轮询请求
    @GetMapping("/poll")
    public DeferredResult<String> pollData(@RequestParam String id) {
        DeferredResult<String> deferredResult = new DeferredResult<>(60_000L, "Timeout");
        
        // 超时处理
        deferredResult.onTimeout(() -> {
            deferredResults.remove(id);
            System.out.println("请求超时，ID: "+ id);
        });

        // 注册请求
        deferredResults.put(id, deferredResult);
        return deferredResult;
    }

    // 外部触发结果（如MQ监听器）
    @GetMapping("/notify")
    public void notifyClient(@RequestParam String id) {
        if (deferredResults.containsKey(id)) {
            deferredResults.get(id).setResult("ok");
            deferredResults.remove(id);
        }
    }
}