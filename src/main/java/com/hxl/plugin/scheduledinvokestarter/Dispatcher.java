package com.hxl.plugin.scheduledinvokestarter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxl.plugin.scheduledinvokestarter.model.InvokeResponseCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.InvokeResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.*;
import org.springframework.test.util.AopTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Dispatcher implements PluginCommunication.MessageCallback {
    private final Logger LOGGER = LoggerFactory.getLogger(SpringRequestMappingCollector.class);
    private Environment environment;
    private ObjectMapper objectMapper;
    private int interceptorIndex;
    private List<HandlerMapping> handlerMappings;
    private boolean parseRequestPath;
    private List<HandlerAdapter> handlerAdapters;

    private SpringRequestMappingCollector springRequestMappingCollector;


    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int part2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;
            if (part1 < part2) {
                return -1;
            } else if (part1 > part2) {
                return 1;
            }
        }
        return 0;
    }

    public Dispatcher(ApplicationContext applicationContext, SpringRequestMappingCollector springRequestMappingCollector) {
        {
            this.springRequestMappingCollector = springRequestMappingCollector;
            environment = applicationContext.getEnvironment();
            objectMapper = applicationContext.getBean(ObjectMapper.class);

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

    @Override
    public void pluginMessage(String msg) {
        try {
            Map<String, Object> taskMap = objectMapper.readValue(msg, new TypeReference<Map<String, Object>>() {
            });
            if (taskMap.getOrDefault("type", "").equals("controller")) invokeController(taskMap);
            if (taskMap.getOrDefault("type", "").equals("scheduled")) invokeSchedule(taskMap);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private boolean applyPreHandle(List<HandlerInterceptor> interceptorList,
                                   Object handler,
                                   HttpServletRequest request, HttpServletResponse response) throws Exception {

        for (int i = 0; i < interceptorList.size(); i++) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            if (!interceptor.preHandle(request, response, handler)) {
                triggerAfterCompletion(interceptorList, handler, request, response, null);
                return false;
            }
            this.interceptorIndex = i;
        }
        return true;
    }

    private void triggerAfterCompletion(List<HandlerInterceptor> interceptorList,
                                        Object handler, HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
        for (int i = interceptorIndex; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            try {
                interceptor.afterCompletion(request, response, handler, ex);
            } catch (Throwable ex2) {
            }
        }
    }

    private void applyPostHandle(List<HandlerInterceptor> interceptorList,
                                 Object handler,
                                 HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
            throws Exception {
        for (int i = interceptorList.size() - 1; i >= 0; i--) {
            HandlerInterceptor interceptor = interceptorList.get(i);
            interceptor.postHandle(request, response, handler, mv);
        }
    }

    /**
     * 插件点击后，将信息推送到这里
     */
    public void invokeController(Map<String, Object> taskMap) {
        try {
            String contentType = taskMap.getOrDefault("contentType", "").toString();
            MockHttpServletRequest mockHttpServletRequest = contentType.toLowerCase().startsWith("multipart/") ?
                    new MockMultipartHttpServletRequest() : new MockHttpServletRequest();

            MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
            String body = taskMap.getOrDefault("body", "").toString();
            boolean useProxyObject = (Boolean) taskMap.getOrDefault("useProxyObject", false);
            boolean interceptor = (boolean) taskMap.getOrDefault("useInterceptor", false);

            String id = taskMap.getOrDefault("id", "").toString();
            String url = taskMap.getOrDefault("url", "").toString();
            Object headers = taskMap.get("headers");
            if (headers instanceof List) {
                List<?> headerList = (List<?>) headers;
                for (Object o : headerList) {
                    if (o instanceof Map) {
                        mockHttpServletRequest.addHeader(((Map<?, ?>) o).get("key").toString().toLowerCase(), ((Map<?, ?>) o).get("value").toString().toLowerCase());

                    }
                }
            }

            mockHttpServletRequest.setCharacterEncoding("utf-8");
            mockHttpServletResponse.setCharacterEncoding("utf-8");

            URI uri = URI.create(url);

            if (contentType.toLowerCase().contains("multipart/form-data")) {
                Object formData = taskMap.get("formData");
                if (formData instanceof List) {
                    for (Object formItem : ((List<?>) formData)) {
                        if (formItem instanceof Map) {
                            Map<?, ?> formItemValue = (Map<?, ?>) formItem;
                            if ("text".equalsIgnoreCase(formItemValue.get("type").toString())) {
                                mockHttpServletRequest.addParameter(formItemValue.get("name").toString(), formItemValue.get("value").toString());
                            }
                            if ("file".equalsIgnoreCase(formItemValue.get("type").toString())) {
                                if (Files.exists(Paths.get(formItemValue.get("value").toString()))) {
                                    String name = formItemValue.get("name").toString();
                                    byte[] value = Files.readAllBytes(Paths.get(formItemValue.get("value").toString()));

                                    mockHttpServletRequest.addPart(new MockPart(name, value));
                                    ((MockMultipartHttpServletRequest) mockHttpServletRequest).addFile(new MockMultipartFile(name, value));
                                }
                            }
                        }
                    }
                }
            } else {
                mockHttpServletRequest.setContent(body.getBytes());
            }
            if (mockHttpServletRequest.getContentType() == null || "".equalsIgnoreCase(mockHttpServletRequest.getContentType())) {
                mockHttpServletRequest.setContentType("application/json");
            }

            ControllerEndpoint endpoint = springRequestMappingCollector.getHandlerMethodMaps().get(id);
            RequestMethodsRequestCondition methodsCondition = endpoint.getRequestMappingInfo().getMethodsCondition();
            Set<RequestMethod> methods = methodsCondition.getMethods();
            RequestMethod requestMethod = methods.isEmpty() ? RequestMethod.GET : methods.stream().findFirst().get();
            mockHttpServletRequest.setMethod(requestMethod.name());


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
            mockHttpServletRequest.setRemoteHost("SpringInvoke");
            mockHttpServletRequest.setRemoteAddr("SpringInvoke");
            mockHttpServletRequest.setRemotePort(6666);
            MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
            for (String queryKey : queryParams.keySet()) {
                mockHttpServletRequest.addParameter(queryKey, queryParams.get(queryKey).toArray(new String[0]));
            }
            if (mockHttpServletRequest.getContentType().contains("www-form-urlencoded")) {
                UriComponents components = UriComponentsBuilder.newInstance()
                        .query(mockHttpServletRequest.getContentAsString())
                        .build();
                MultiValueMap<String, String> bodyQueryParams = components.getQueryParams();
                for (String queryKey : bodyQueryParams.keySet()) {
                    mockHttpServletRequest.setParameter(queryKey, bodyQueryParams.get(queryKey).toArray(new String[0]));
                }
            }

            ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(mockHttpServletRequest);
            RequestContextHolder.setRequestAttributes(servletRequestAttributes);
            if (this.parseRequestPath) {
                ServletRequestPathUtils.parseAndCache(mockHttpServletRequest);
            }
            HandlerExecutionChain mappedHandler = getHandler(mockHttpServletRequest);

            if (mappedHandler != null) {
                HandlerMethod handlerMethod = endpoint.getHandlerMethod();
                HandlerMethod withResolvedBean = handlerMethod.createWithResolvedBean();

                Object targetObject = AopTestUtils.getTargetObject(withResolvedBean.getBean());

                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
                Object handler = mappedHandler.getHandler();

                LOGGER.info("invoke {} use {} object", withResolvedBean.getBeanType(), useProxyObject ? "proxy" : "source");
                if (!useProxyObject) {
                    if (handler instanceof HandlerMethod) {
                        Field beanField = HandlerMethod.class.getDeclaredField("bean");
                        beanField.setAccessible(true);
                        beanField.set(handler, targetObject);
                    }
                }
                HandlerInterceptor[] interceptors = mappedHandler.getInterceptors();

                List<HandlerInterceptor> interceptorList = interceptors == null ? new ArrayList<>() : Arrays.asList(interceptors);

                LOGGER.info("invoke: {}->{}", withResolvedBean.getMethod().getDeclaringClass().getName(), withResolvedBean.getMethod().getName());
                LOGGER.info("content-type: {}", mockHttpServletRequest.getContentType());
                LOGGER.info("post body: {}", body);
                LOGGER.info("request url: {}", mockHttpServletRequest.getRequestURI());

                if (interceptor) {
                    if (!applyPreHandle(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse)) {
                        LOGGER.info("use interceptor ");
                        LOGGER.info("response value: {}", mockHttpServletResponse.getContentAsString());
                        sendResponse(mockHttpServletResponse, id);
                        return;
                    }
                }
                ha.handle(mockHttpServletRequest, mockHttpServletResponse, handler);//invoke
                applyPostHandle(interceptorList, handler, mockHttpServletRequest, mockHttpServletResponse, new ModelAndView());
                LOGGER.info("response value: {}", mockHttpServletResponse.getContentAsString());
                LOGGER.info("response content-type: {}", mockHttpServletResponse.getContentType());
                sendResponse(mockHttpServletResponse, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isTextContentType(String contentType) {
        if (contentType != null) {
            String lowerCaseContentType = contentType.toLowerCase();
            // 判断是否为文本类型
            if (lowerCaseContentType.startsWith("text/") ||
                    lowerCaseContentType.contains("application/json") ||
                    lowerCaseContentType.contains("application/xml") ||
                    lowerCaseContentType.contains("application/xhtml+xml") ||
                    lowerCaseContentType.contains("urlencoded")) {
                return true;
            }
        }
        return false;
    }

    private void sendResponse(MockHttpServletResponse response, String requestId) {
        try {
            List<InvokeResponseModel.Header> headers = new ArrayList<>();
            for (String headerName : response.getHeaderNames()) {
                for (String value : response.getHeaders(headerName)) {
                    headers.add(new InvokeResponseModel.Header(headerName, value));
                }
            }
            InvokeResponseModel invokeResponseModel = InvokeResponseModel.InvokeResponseModelBuilder.anInvokeResponseModel()
                    .withData(response.getContentAsString(StandardCharsets.UTF_8))
                    .withId(requestId)
                    .withHeader(headers)
                    .build();
            PluginCommunication.send(new InvokeResponseCommunicationPackage(invokeResponseModel));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    @Nullable
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        if (this.handlerMappings != null) {
            for (HandlerMapping mapping : this.handlerMappings) {
                HandlerExecutionChain handler = mapping.getHandler(request);
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
            if (springRequestMappingCollector.getScheduledEndpointMap().containsKey(id)) {
                ScheduledEndpoint scheduledEndpoint = springRequestMappingCollector.getScheduledEndpointMap().get(id);
                LOGGER.info("invoke scheduled {}.{}", scheduledEndpoint.getMethod().getDeclaringClass().getName(), scheduledEndpoint.getMethod().getName());
                scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
