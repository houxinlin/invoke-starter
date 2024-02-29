package com.hxl.plugin.scheduledinvokestarter.components.spring.controller;

import com.hxl.plugin.scheduledinvokestarter.*;
import com.hxl.plugin.scheduledinvokestarter.compatible.VersionInstance;
import com.hxl.plugin.scheduledinvokestarter.model.InvokeReceiveModel;
import com.hxl.plugin.scheduledinvokestarter.model.InvokeResponseModel;
import com.hxl.plugin.scheduledinvokestarter.model.pack.InvokeResponseCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ReceiveCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.utils.AopUtilsAdapter;
import com.hxl.plugin.scheduledinvokestarter.utils.CoolRequestStarConfig;
import com.hxl.plugin.scheduledinvokestarter.utils.VersionUtils;
import com.hxl.plugin.scheduledinvokestarter.utils.exception.InvokeException;
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
    private final SpringRequestMappingComponent springRequestMappingComponent;
    private RequestToViewNameTranslator viewNameTranslator;

    public Dispatcher(ApplicationContext applicationContext, SpringRequestMappingComponent springRequestMappingComponent) {
        {
            this.springRequestMappingComponent = springRequestMappingComponent;
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
            if (!VersionInstance.invokeHandlerInterceptor_preHandle(interceptor, request, response, handler)) {
                triggerAfterCompletion(interceptorList, handler, request, response, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    private void triggerAfterCompletion(List<HandlerInterceptor> interceptorList,
                                        Object handler, Object request, Object response, @Nullable Exception ex) {
        for (int i = interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            try {
                VersionInstance.invokeHandlerInterceptor_afterCompletion(interceptor, request, response, handler, ex);
            } catch (Throwable ex2) {
            }
        }
    }

    private void applyPostHandle(List<HandlerInterceptor> interceptorList,
                                 Object handler,
                                 Object request, Object response, @Nullable ModelAndView mv)
            throws Exception {
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            VersionInstance.invokeHandlerInterceptor_postHandle(interceptor, request, response, handler, mv);
//            interceptor.postHandle(request, response, handler, mv);
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

    private void doInvokeController(ControllerRequestData requestData, int serverPort) {
        MockHttpServletResponse mockHttpServletResponse = createMockHttpServletResponse();
        MockHttpServletRequest mockHttpServletRequest = null;
        Exception exception = null;
        HandlerExecutionChain mappedHandler = null;
        List<HandlerInterceptor> interceptorList = null;
        Object handler = null;

        try {
            String contentType = Optional.ofNullable(requestData.getContentType()).orElse("application/json");
            mockHttpServletRequest = createMockHttpServletRequest(contentType);
            String body = Optional.ofNullable(requestData.getBody()).orElse("");
            String url = Optional.ofNullable(requestData.getUrl()).orElse("/");

            for (KeyValue keyValue : Optional.ofNullable(requestData.getHeaders()).orElse(new ArrayList<>())) {
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
                            VersionInstance.invokeHttpServletRequest_addPart(mockHttpServletRequest, new MockPart(name, value));
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

            ControllerEndpoint endpoint = springRequestMappingComponent.getHandlerMethodMaps().get(requestData.getId());
            if (endpoint == null) {
                throw new InvokeException("Unable to call:" + requestData.getUrl());
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
            mockHttpServletRequest.setRemotePort(6666);
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

            ServletRequestAttributes servletRequestAttributes = VersionInstance.newServletRequestAttributes(mockHttpServletRequest);
            RequestContextHolder.setRequestAttributes(servletRequestAttributes);
            if (this.parseRequestPath) {
                VersionInstance.invokeServletRequestPathUtils_parseAndCache(mockHttpServletRequest);
            }
            mappedHandler = getHandler(mockHttpServletRequest);

            if (mappedHandler != null) {
                HandlerMethod handlerMethod = endpoint.getHandlerMethod();
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
                        return;
                    }
                }
                ModelAndView mv = VersionInstance.invokeHandlerAdapter_handle(ha, mockHttpServletRequest, mockHttpServletResponse, handler);
                if (requestData.isUseInterceptor()) {
                    applyPostHandle(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse, new ModelAndView());
                }
            }
        } catch (Exception e) {
            if (CoolRequestStarConfig.isDebug()) {
                e.printStackTrace();
            }
            exception = e;
        } finally {
            responseToPlugin(mockHttpServletResponse, requestData.getId(), exception);
            if (requestData.isUseInterceptor()) {
                triggerAfterCompletion(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse, null);
            }
        }
    }

    public void invokeController(ControllerRequestData requestData, int serverPort) {
        InvokeReceiveModel invokeReceiveModel = new InvokeReceiveModel();
        invokeReceiveModel.setRequestId(requestData.getId());
        PluginCommunication.send(new ReceiveCommunicationPackage(invokeReceiveModel));
        doInvokeController(requestData, serverPort);
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

    private void responseToPlugin(MockHttpServletResponse response, String requestId, Exception exception) {
        if (exception != null) {
            PluginCommunication.send(new InvokeResponseCommunicationPackage(new ExceptionInvokeResponseModel(requestId, exception)));
            return;
        }
        List<InvokeResponseModel.Header> headers = new ArrayList<>();
        for (String headerName : response.getHeaderNames()) {
            for (String value : response.getHeaders(headerName)) {
                headers.add(new InvokeResponseModel.Header(headerName, value));
            }
        }
        byte[] contentAsByteArray = response.getContentAsByteArray();
        String body = contentAsByteArray != null ? Base64.getEncoder().encodeToString(contentAsByteArray) : "";
        InvokeResponseModel invokeResponseModel = InvokeResponseModel.InvokeResponseModelBuilder.anInvokeResponseModel()
                .withData(body)
                .withId(requestId)
                .withHeader(headers)
                .build();
        invokeResponseModel.setCode(response.getStatus());
        PluginCommunication.send(new InvokeResponseCommunicationPackage(invokeResponseModel));
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
                HandlerExecutionChain handler = VersionInstance.invokeHandlerMapping_getHandler(mapping, request);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    public void invokeSchedule(Map<String, Object> taskMap) {
        String id = taskMap.getOrDefault("id", "").toString();
        try {
            if (springRequestMappingComponent.getScheduledEndpointMap().containsKey(id)) {
                ScheduledEndpoint scheduledEndpoint = springRequestMappingComponent.getScheduledEndpointMap().get(id);
                LOGGER.info("invoke scheduled {}.{}", scheduledEndpoint.getMethod().getDeclaringClass().getName(), scheduledEndpoint.getMethod().getName());
                scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
