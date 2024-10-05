package com.cool.request.components.spring.controller;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.compatible.CompatibilityUtil;
import com.cool.request.components.http.*;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.utils.AopUtilsAdapter;
import com.cool.request.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class Dispatcher {
    private final Logger LOGGER = LoggerFactory.getLogger(SpringRequestMappingComponent.class);
    private final Environment environment;
    private int interceptorIndex;
    private List<HandlerMapping> handlerMappings;
    private boolean parseRequestPath;
    private List<HandlerAdapter> handlerAdapters;

    public Dispatcher(ApplicationContext applicationContext) {
        {
            this.environment = applicationContext.getEnvironment();

            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
            if (VersionUtils.is5Dot3()) {
                for (HandlerMapping mapping : this.handlerMappings) {
                    if (mapping.usesPathPatterns()) {
                        this.parseRequestPath = true;
                        break;
                    }
                }
            }
        }
        {
            Map<String, HandlerAdapter> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                AnnotationAwareOrderComparator.sort(this.handlerAdapters);
            }
        }

    }

    private boolean applyPreHandle(List<HandlerInterceptor> interceptorList,
                                   Object handler,
                                   Object request, Object response) {

        for (int i = 0; i < interceptorList.size(); i++) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            if (!CompatibilityUtil.invokeHandlerInterceptor_preHandle(interceptor, request, response, handler)) {
                triggerAfterCompletion(interceptorList, handler, request, response);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    private void triggerAfterCompletion(List<HandlerInterceptor> interceptorList,
                                        Object handler, Object request, Object response) {
        for (int i = interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            try {
                CompatibilityUtil.invokeHandlerInterceptor_afterCompletion(interceptor, request, response, handler, null);
            } catch (Throwable ignored) {
            }
        }
    }

    private void applyPostHandle(List<HandlerInterceptor> interceptorList,
                                 Object handler,
                                 Object request, Object response, @Nullable ModelAndView mv) {
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            CompatibilityUtil.invokeHandlerInterceptor_postHandle(interceptor, request, response, handler, mv);
        }
    }


    private MockHttpServletRequest createMockHttpServletRequest(String contentType) {
        MockHttpServletRequest mockHttpServletRequest = contentType.toLowerCase().startsWith("multipart/") ?
                new MockMultipartHttpServletRequest() : new MockHttpServletRequest();
        mockHttpServletRequest.setCharacterEncoding("utf-8");
        mockHttpServletRequest.setContentType(contentType);
        return mockHttpServletRequest;
    }

    private MockHttpServletResponse createMockHttpServletResponse() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return mockHttpServletResponse;
    }

    public static int generateRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private InvokeResponseModel doInvokeController(ReflexHttpRequestParamAdapterBody requestData, int serverPort) {
        MockHttpServletResponse mockHttpServletResponse = createMockHttpServletResponse();
        MockHttpServletRequest mockHttpServletRequest = null;
        Exception exception = null;
        HandlerExecutionChain mappedHandler = null;
        List<HandlerInterceptor> interceptorList = null;
        Object handler = null;
        InvokeResponseModel invokeResponseModel = null;
        try {
            String contentType = Optional.ofNullable(requestData.getContentType()).orElse("application/json");
            mockHttpServletRequest = createMockHttpServletRequest(contentType);
            String body = Optional.ofNullable(requestData.getBody()).orElse("");
            String url = Optional.ofNullable(requestData.getUrl()).orElse("/");

            for (com.cool.request.components.http.KeyValue keyValue : Optional.ofNullable(requestData.getHeaders()).orElse(new ArrayList<>())) {
                mockHttpServletRequest.addHeader(keyValue.getKey().toLowerCase(), keyValue.getValue().toLowerCase());
            }

            mockHttpServletRequest.setServerPort(serverPort);
            URI uri = URI.create(url);
            if (contentType.toLowerCase().contains("multipart/form-data")) {
                for (FormDataInfo formDataInfo : Optional.ofNullable(requestData.getFormData()).orElse(new ArrayList<>())) {
                    if ("file".equalsIgnoreCase(formDataInfo.getType())) {
                        String valueItem = formDataInfo.getValue();
                        Path filePath = Paths.get(valueItem);
                        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                            String name = formDataInfo.getName();
                            byte[] value = Files.readAllBytes(filePath);
                            CompatibilityUtil.invokeHttpServletRequest_addPart(mockHttpServletRequest, new MockPart(name, value));
                            MockMultipartFile mockMultipartFile = new MockMultipartFile(name, filePath.toFile().getName(), probeContentType(filePath), value);
                            ((MockMultipartHttpServletRequest) mockHttpServletRequest).addFile(mockMultipartFile);
                        } else {
                            LOGGER.error("invalid file path:" + filePath);
                        }
                    } else {
                        mockHttpServletRequest.addParameter(formDataInfo.getName(), formDataInfo.getValue());
                    }
                }

            } else {
                if ("application/octet-stream".equals(contentType)) {
                    Path path = Paths.get(body);
                    if (!Files.exists(path)) {
                        throw new IOException("file does not exist" + path);
                    }
                    byte[] bytes = StreamUtils.copyToByteArray(Files.newInputStream(path));
                    mockHttpServletRequest.setContent(bytes);

                } else {
                    mockHttpServletRequest.setContent(body.getBytes());
                }
            }
            mockHttpServletRequest.setMethod(requestData.getMethod());
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri);
            UriComponents uriComponents = uriComponentsBuilder.build();
            mockHttpServletRequest.setProtocol(uri.getScheme());
            mockHttpServletRequest.setRequestURI(uriComponents.getPath());
            mockHttpServletRequest.setQueryString(uriComponents.getQuery());
            String springContextPath = environment.getProperty("server.servlet.context-path");
            if (springContextPath == null) springContextPath = "/";

            mockHttpServletRequest.setContextPath(springContextPath);
            mockHttpServletRequest.setServletPath(uri.getPath());
            if (!"/".equals(springContextPath) && uri.getPath().startsWith(springContextPath)) {
                mockHttpServletRequest.setServletPath(uri.getPath().replaceFirst("^" + springContextPath, ""));
            }
            mockHttpServletRequest.setRemoteHost("127.0.0.1");
            mockHttpServletRequest.setRemoteAddr("127.0.0.1");
            mockHttpServletRequest.setRemotePort(generateRandomNumber(1024, 65535));
            MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
            for (String queryKey : queryParams.keySet()) {
                mockHttpServletRequest.addParameter(queryKey, urlDecode(queryParams.get(queryKey)).toArray(new String[0]));
            }
            if (mockHttpServletRequest.getContentType().contains("www-form-urlencoded")) {
                UriComponents components = UriComponentsBuilder.newInstance()
                        .query(body)
                        .build();
                MultiValueMap<String, String> bodyQueryParams = components.getQueryParams();
                for (String queryKey : bodyQueryParams.keySet()) {
                    mockHttpServletRequest.setParameter(queryKey, urlDecode(bodyQueryParams.get(queryKey)).toArray(new String[0]));
                }
            }

            ServletRequestAttributes servletRequestAttributes = CompatibilityUtil.newServletRequestAttributes(mockHttpServletRequest);
            RequestContextHolder.setRequestAttributes(servletRequestAttributes);
            if (this.parseRequestPath) {
                CompatibilityUtil.invokeServletRequestPathUtils_parseAndCache(mockHttpServletRequest);
            }
            mappedHandler = getHandler(mockHttpServletRequest);

            if (mappedHandler != null) {
                if (mappedHandler.getHandler() instanceof HandlerMethod) {
                    HandlerMethod handlerMethod = (HandlerMethod) mappedHandler.getHandler();
                    HandlerMethod withResolvedBean = handlerMethod.createWithResolvedBean();
                    Object targetObject = AopUtilsAdapter.getTargetObject(withResolvedBean.getBean());
                    HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
                    handler = mappedHandler.getHandler();

                    if (!requestData.isUseProxyObject()) {
                        if (handler instanceof HandlerMethod) {
                            Field beanField = HandlerMethod.class.getDeclaredField("bean");
                            beanField.setAccessible(true);
                            beanField.set(handler, targetObject);
                        }
                    }
                    HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();
                    interceptorList = interceptors == null ? new ArrayList<>() : Arrays.asList(interceptors);

                    if (requestData.isUseInterceptor()) {
                        if (!applyPreHandle(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse)) {
                            return responseToPlugin(mockHttpServletResponse, requestData, exception, mappedHandler);
                        }
                    }
                    CompatibilityUtil.invokeHandlerAdapter_handle(ha, mockHttpServletRequest, mockHttpServletResponse, handler);
                    if (requestData.isUseInterceptor()) {
                        applyPostHandle(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse, new ModelAndView());
                    }
                }

            } else {
                CoolRequestProjectLog.log("无法找到mappedHandler：" + requestData.getUrl());
            }
        } catch (Exception e) {
            CoolRequestProjectLog.userExceptionLog(e);
            exception = e;
        } finally {
            invokeResponseModel = responseToPlugin(mockHttpServletResponse, requestData, exception, mappedHandler);
            if (requestData.isUseInterceptor()) {
                triggerAfterCompletion(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse);
            }

        }
        return invokeResponseModel;
    }

    /**
     * 通过反射进入到这里x,不要删除
     */
    public InvokeResponseModel invokeController(ReflexHttpRequestParamAdapterBody requestData, int serverPort) {
        return doInvokeController(requestData, serverPort);
    }

    private String probeContentType(Path path) {
        if (Files.isDirectory(path)) return "";
        try {
            return Files.probeContentType(path);
        } catch (IOException ignored) {
        }
        return "application/stream";
    }

    private List<String> urlDecode(List<String> values) {
        return values.stream().map(s -> {
            try {
                return URLDecoder.decode(s, "utf-8");
            } catch (UnsupportedEncodingException ignored) {
            }
            return s;
        }).collect(Collectors.toList());
    }

    private InvokeResponseModel responseToPlugin(MockHttpServletResponse response,
                                                 ReflexHttpRequestParamAdapterBody controllerRequestData,
                                                 Exception exception, HandlerExecutionChain mappedHandler) {
        String requestId = controllerRequestData.getId();

        if (mappedHandler != null) {
            if (!(mappedHandler.getHandler() instanceof HandlerMethod)) {
                return new HandlerMethodNotFoundExceptionInvokeResponseModel(requestId, new IllegalArgumentException("Not Found Method Handler"));
            }
        }
        if (exception != null) {
            return new ExceptionInvokeResponseModel(requestId, exception);
        }
        List<Header> headers = new ArrayList<>();
        for (String headerName : response.getHeaderNames()) {
            for (String value : response.getHeaders(headerName)) {
                headers.add(new Header(headerName, value));
            }
        }
        byte[] contentAsByteArray = response.getContentAsByteArray();
        String body = Base64.getEncoder().encodeToString(contentAsByteArray);
        InvokeResponseModel invokeResponseModel = InvokeResponseModel.InvokeResponseModelBuilder.anInvokeResponseModel()
                .withData(body)
                .withId(requestId)
                .withHeader(headers)
                .build();
        invokeResponseModel.setCode(response.getStatus());
        invokeResponseModel.setAttachData(controllerRequestData.getAttachData());
        return invokeResponseModel;
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws Exception {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }
        throw new Exception("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    @Nullable
    protected HandlerExecutionChain getHandler(Object request) {
        if (this.handlerMappings != null) {
            for (HandlerMapping mapping : this.handlerMappings) {
                HandlerExecutionChain handler = CompatibilityUtil.invokeHandlerMapping_getHandler(mapping, request);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }
}
