package com.alec.InnovateX.spring;

import com.alec.InnovateX.spring.mvc.LoggingInterceptor;
import com.alec.InnovateX.spring.mvc.MvcWebConfig;
import com.alec.InnovateX.spring.mvc.Person;
import com.alec.InnovateX.spring.mvc.PersonRestClient;
import com.alec.InnovateX.spring.mvc.TimingFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 主题⑮Spring MVC：DispatcherServlet 请求流程、Filter vs Interceptor 顺序、
 * @RestControllerAdvice 全局异常、内容协商与自定义 HttpMessageConverter、
 * 自定义 HandlerMethodArgumentResolver、Callable/SSE 异步。
 * 同步请求直接手工驱动 DispatcherServlet（不依赖 Boot/内嵌容器），异步用 MockMvc 驱动 dispatch 循环
 */
public class MvcDeepTest {

    /** 手工组装的"迷你 Web 应用"：WebApplicationContext + DispatcherServlet */
    private static final class MvcApp implements AutoCloseable {

        final AnnotationConfigWebApplicationContext context;

        final DispatcherServlet servlet;

        private MvcApp() {
            MockServletContext servletContext = new MockServletContext();
            context = new AnnotationConfigWebApplicationContext();
            context.setServletContext(servletContext);
            context.register(MvcWebConfig.class);
            context.refresh();
            servlet = new DispatcherServlet(context);
            try {
                // init 阶段完成 DispatcherServlet 的策略初始化（HandlerMapping/HandlerAdapter/异常解析器…）
                servlet.init(new MockServletConfig(servletContext));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        MockHttpServletResponse dispatch(MockHttpServletRequest request) throws Exception {
            MockHttpServletResponse response = new MockHttpServletResponse();
            servlet.service(request, response);
            return response;
        }

        @Override
        public void close() {
            context.close();
        }
    }

    @Test
    public void dispatcherServletFlow() throws Exception {
        try (MvcApp app = new MvcApp()) {
            // GET + 路径变量：HandlerMapping 找到处理器 -> HandlerAdapter 调用 -> Jackson 写响应
            MockHttpServletResponse resp = app.dispatch(new MockHttpServletRequest("GET", "/api/persons/1"));
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentType().startsWith("application/json"));
            assertTrue(resp.getContentAsString().contains("\"name\":\"InnovateX-User-1\""));
            System.out.println("GET /api/persons/1 -> " + resp.getContentAsString());

            // POST + @RequestBody：请求体经 HttpMessageConverter 反序列化成 Person
            MockHttpServletRequest post = new MockHttpServletRequest("POST", "/api/persons");
            post.setContentType("application/json");
            post.setContent("{\"id\":9,\"name\":\"InnovateX\"}".getBytes(StandardCharsets.UTF_8));
            resp = app.dispatch(post);
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("已创建:InnovateX"));
            System.out.println("POST /api/persons -> " + resp.getContentAsString());
        }
    }

    @Test
    public void filterAndInterceptorOrder() throws Exception {
        LoggingInterceptor.EVENTS.clear();
        try (MvcApp app = new MvcApp()) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/persons/2");
            MockHttpServletResponse response = new MockHttpServletResponse();
            // Filter 包在 DispatcherServlet 外面：链的末端就是 servlet 本身
            FilterChain chainToEndServlet = (req, res) -> app.servlet.service(req, res);
            new TimingFilter().doFilter(request, response, chainToEndServlet);

            assertEquals(200, response.getStatus());
            // 完整顺序：Filter 进 -> preHandle -> 处理器 -> postHandle -> afterCompletion -> Filter 出
            assertEquals(List.of(
                    "filter-before:/api/persons/2",
                    "interceptor-preHandle:/api/persons/2",
                    "interceptor-postHandle:status=200",
                    "interceptor-afterCompletion:无异常"
            ), LoggingInterceptor.EVENTS.subList(0, 4));
            assertEquals(5, LoggingInterceptor.EVENTS.size());
            assertTrue(LoggingInterceptor.EVENTS.get(4).startsWith("filter-after:"));
            System.out.println("Filter 与 Interceptor 执行顺序: " + LoggingInterceptor.EVENTS);
        }
    }

    @Test
    public void customArgumentResolver() throws Exception {
        try (MvcApp app = new MvcApp()) {
            // 请求头被解析成 ClientContext，控制器方法直接声明该类型参数
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/client");
            request.addHeader("X-Client-Id", "web-client-7");
            request.addHeader("X-Forwarded-For", "10.0.0.9");
            MockHttpServletResponse resp = app.dispatch(request);
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("web-client-7"));
            assertTrue(resp.getContentAsString().contains("10.0.0.9"));
            System.out.println("自定义参数解析: " + resp.getContentAsString());

            // 没有请求头时走解析器里的默认值
            resp = app.dispatch(new MockHttpServletRequest("GET", "/api/client"));
            assertTrue(resp.getContentAsString().contains("anonymous"));
            System.out.println("缺省头解析: " + resp.getContentAsString());
        }
    }

    @Test
    public void contentNegotiationWithCustomConverter() throws Exception {
        try (MvcApp app = new MvcApp()) {
            // Accept: text/csv -> 命中自定义的 PersonCsvHttpMessageConverter
            MockHttpServletRequest csv = new MockHttpServletRequest("GET", "/api/persons/1");
            csv.addHeader("Accept", "text/csv");
            MockHttpServletResponse resp = app.dispatch(csv);
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentType().startsWith("text/csv"));
            assertEquals("1,InnovateX-User-1", resp.getContentAsString());
            System.out.println("Accept: text/csv -> " + resp.getContentAsString());

            // Accept: application/json（或不发 Accept）-> Jackson
            MockHttpServletRequest json = new MockHttpServletRequest("GET", "/api/persons/1");
            json.addHeader("Accept", "application/json");
            resp = app.dispatch(json);
            assertTrue(resp.getContentAsString().contains("\"id\":1"));
            System.out.println("Accept: application/json -> " + resp.getContentAsString());
        }
    }

    @Test
    public void globalExceptionHandling() throws Exception {
        LoggingInterceptor.EVENTS.clear();
        try (MvcApp app = new MvcApp()) {
            // 业务异常被 @RestControllerAdvice 接住：状态与响应体都由 advice 决定
            MockHttpServletResponse resp = app.dispatch(new MockHttpServletRequest("GET", "/api/error"));
            assertEquals(418, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("\"code\":\"BIZ_001\""));
            System.out.println("业务异常 -> " + resp.getStatus() + " " + resp.getContentAsString());

            // 未精确匹配的异常走 advice 的 Exception 兜底方法
            resp = app.dispatch(new MockHttpServletRequest("GET", "/api/boom"));
            assertEquals(500, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("INTERNAL"));
            System.out.println("兜底异常 -> " + resp.getStatus() + " " + resp.getContentAsString());

            // 异常路径跳过 postHandle（处理器没"正常返回"）
            assertTrue(LoggingInterceptor.EVENTS.stream().noneMatch(e -> e.startsWith("interceptor-postHandle")));
            // 反直觉但合理：异常被 @RestControllerAdvice 成功处理后，afterCompletion 收到的是 null——
            // 只有异常"没有任何解析器处理、继续上抛"时，afterCompletion 才会拿到真实异常
            assertEquals(2, LoggingInterceptor.EVENTS.stream()
                    .filter(e -> e.equals("interceptor-afterCompletion:无异常")).count());
            System.out.println("被 advice 处理的异常: afterCompletion 拿到 ex=null（事件: "
                    + LoggingInterceptor.EVENTS + "）");
        }
    }

    @Test
    public void asyncCallableAndSse() throws Exception {
        // 异步请求需要 MockMvc 驱动完整的 async dispatch 循环（standalone 装配，不依赖上面的 MVC 配置）
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new com.alec.InnovateX.spring.mvc.DemoController()).build();

        // Callable：容器线程立即释放，业务在工作线程执行后 async dispatch 回来完成响应
        MvcResult callable = mockMvc.perform(get("/api/async"))
                .andExpect(request().asyncStarted())
                .andReturn();
        String body = mockMvc.perform(asyncDispatch(callable))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(body.startsWith("async-result@["));
        System.out.println("Callable 异步结果: " + body);

        // SSE：一次连接服务端推送多批事件（text/event-stream）
        MvcResult sse = mockMvc.perform(get("/api/sse"))
                .andExpect(request().asyncStarted())
                .andReturn();
        String sseBody = mockMvc.perform(asyncDispatch(sse))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data:event-1")))
                .andReturn().getResponse().getContentAsString();
        assertTrue(sseBody.contains("data:event-3"));
        System.out.println("SSE 响应体:\n" + sseBody);
    }

    @Test
    public void validationWithInitBinder() throws Exception {
        try (MvcApp app = new MvcApp()) {
            // 校验失败（处理器自带 BindingResult）：不抛异常，手动返回错误清单
            MockHttpServletRequest invalid = new MockHttpServletRequest("POST", "/api/validate");
            invalid.setContentType("application/x-www-form-urlencoded");
            invalid.addParameter("name", "");
            invalid.addParameter("age", "-1");
            MockHttpServletResponse resp = app.dispatch(invalid);
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("姓名不能为空"));
            assertTrue(resp.getContentAsString().contains("年龄不能为负数"));
            System.out.println("BindingResult 自处理: " + resp.getContentAsString());

            // 校验通过
            MockHttpServletRequest valid = new MockHttpServletRequest("POST", "/api/validate");
            valid.setContentType("application/x-www-form-urlencoded");
            valid.addParameter("name", "InnovateX");
            valid.addParameter("age", "1");
            resp = app.dispatch(valid);
            assertTrue(resp.getContentAsString().contains("\"valid\":true"));
            System.out.println("校验通过: " + resp.getContentAsString());

            // strict 端点没有 BindingResult 参数：校验失败抛 BindException -> advice 转 400
            MockHttpServletRequest strict = new MockHttpServletRequest("POST", "/api/validate-strict");
            strict.setContentType("application/x-www-form-urlencoded");
            strict.addParameter("name", "");
            resp = app.dispatch(strict);
            assertEquals(400, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("VALIDATION_FAILED"));
            System.out.println("BindException 全局处理: " + resp.getStatus() + " " + resp.getContentAsString());
        }
    }

    @Test
    public void maskedReturnValueHandler() throws Exception {
        try (MvcApp app = new MvcApp()) {
            MockHttpServletResponse resp = app.dispatch(new MockHttpServletRequest("GET", "/api/card"));
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentType().startsWith("text/plain"));
            assertEquals("****4567", resp.getContentAsString());
            System.out.println("自定义返回值处理器脱敏: 6222020001234567 -> " + resp.getContentAsString());
        }
    }

    @Test
    public void multipartUpload() throws Exception {
        try (MvcApp app = new MvcApp()) {
            MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
            request.setMethod("POST");
            request.setRequestURI("/api/upload");
            request.addFile(new MockMultipartFile("file", "hello.txt", "text/plain",
                    "InnovateX 上传内容".getBytes(StandardCharsets.UTF_8)));
            MockHttpServletResponse resp = app.dispatch(request);
            assertEquals(200, resp.getStatus());
            assertTrue(resp.getContentAsString().contains("hello.txt"));
            assertTrue(resp.getContentAsString().contains("InnovateX 上传内容"));
            System.out.println("multipart 上传: " + resp.getContentAsString());
        }
    }

    @Test
    public void restTemplateWithMockServer() {
        RestTemplate restTemplate = new RestTemplate();
        // MockRestServiceServer 拦截 RestTemplate 的请求：不发真实网络请求即可验证客户端行为
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("/api/persons/7"))
                .andRespond(withSuccess("{\"id\":7,\"name\":\"RestTemplate-Mock\"}", MediaType.APPLICATION_JSON));

        Person person = new PersonRestClient(restTemplate).fetchPerson("/api/persons/7");
        assertEquals(7, person.id());
        assertEquals("RestTemplate-Mock", person.name());
        server.verify();
        System.out.println("RestTemplate + MockRestServiceServer: " + person);
    }
}
