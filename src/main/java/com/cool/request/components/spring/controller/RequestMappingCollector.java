package com.cool.request.components.spring.controller;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.Controller;
import com.cool.request.components.http.DynamicController;
import com.cool.request.utils.VersionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.stream.Collectors;

import static com.cool.request.utils.SpringUtils.getContextPath;
import static com.cool.request.utils.SpringUtils.getServerPort;

public class RequestMappingCollector implements ControllerCollector {
    @Override
    public List<DynamicController> collect(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        List<DynamicController> result = new ArrayList<>();
        Map<String, RequestMappingHandlerMapping> beansOfType = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        for (RequestMappingHandlerMapping requestMappingHandlerMapping : beansOfType.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            parseController(applicationContext, result, handlerMethods, springBootStartInfo);
        }
        return result;
    }

    protected void parseController(ApplicationContext applicationContext,
                                   List<DynamicController> controllers,
                                   Map<RequestMappingInfo, HandlerMethod> handlerMethods,
                                   SpringBootStartInfo springBootStartInfo) {
        String contextPath = getContextPath(applicationContext);
        int serverPort = getServerPort(applicationContext);
        for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {
            HandlerMethod handlerMethod = handlerMethods.get(requestMappingInfo);
            for (String url : getUrlPattern(requestMappingInfo)) {
                RequestMethod requestMethod = requestMappingInfo.getMethodsCondition().getMethods().stream().findFirst().orElse(RequestMethod.GET);
                DynamicController dynamicController = (DynamicController) Controller.ControllerBuilder
                        .aController()
                        .withContextPath(contextPath)
                        .withHttpMethod(requestMethod.name())
                        .withMethodName(handlerMethod.getMethod().getName())
                        .withUrl(url)
                        .withServerPort(serverPort)
                        .withSimpleClassName(handlerMethod.getBeanType().getName())
                        .build(new DynamicController());
                dynamicController.setParamClassList(getParamClassList(handlerMethod));
                dynamicController.setSpringBootStartPort(springBootStartInfo.getAvailableTcpPort());
                controllers.add(dynamicController);
            }
        }
    }

    private List<String> getParamClassList(HandlerMethod handlerMethod) {
        try {
            if (VersionUtils.isSpring5()) {
                return Arrays.stream(handlerMethod.getMethodParameters())
                        .map(methodParameter -> methodParameter.getParameter().getType().getName()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            CoolRequestProjectLog.logWithDebug(e);
        }
        return Arrays.stream(handlerMethod.getMethod().getParameterTypes()).map(Class::getSimpleName).collect(Collectors.toList());
    }

    private boolean hasMethod(Class<?> targetClass, String methodName, Class<?> retClass, Class<?>... ptypes) {
        try {
            MethodHandles.lookup().findVirtual(targetClass, methodName, MethodType.methodType(retClass, ptypes));
            return true;
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        return false;

    }

    private Set<String> getUrlPattern(RequestMappingInfo requestMappingInfo) {
        try {
            if (hasMethod(requestMappingInfo.getClass(), "getPatternValues", Set.class)) {
                return Optional.of(requestMappingInfo.getPatternValues()).orElse(new HashSet<>());
            }
            PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
            if (patternsCondition != null) {
                return patternsCondition.getPatterns();
            }
            PathPatternsRequestCondition pathPatternsCondition = requestMappingInfo.getPathPatternsCondition();
            if (pathPatternsCondition != null) {
                return pathPatternsCondition.getPatternValues();
            }
        } catch (Exception ignored) {
        }
        return new HashSet<>();
    }

}

