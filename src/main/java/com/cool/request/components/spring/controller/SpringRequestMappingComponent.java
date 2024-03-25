package com.cool.request.components.spring.controller;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.MockClassLoader;
import com.cool.request.ScheduledEndpoint;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.Controller;
import com.cool.request.components.http.DynamicController;
import com.cool.request.components.http.ExceptionInvokeResponseModel;
import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.components.scheduled.DynamicSpringScheduled;
import com.cool.request.components.scheduled.ScheduledListener;
import com.cool.request.json.JsonMapper;
import com.cool.request.utils.AnnotationUtilsAdapter;
import com.cool.request.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cool.request.utils.SpringUtils.getContextPath;
import static com.cool.request.utils.SpringUtils.getServerPort;


public class SpringRequestMappingComponent implements
        ComponentDataHandler,
        ControllerInvokeListener, ScheduledListener {
    private final Logger LOGGER = LoggerFactory.getLogger(SpringRequestMappingComponent.class);
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private final ApplicationContext applicationContext;
    private final List<DynamicSpringScheduled> scheduledInvokeBeans = new ArrayList<>();
    private final SpringBootStartInfo springBootStartInfo;
    private boolean isScanning = false;
    public static JsonMapper jsonMapper;
    private boolean refreshing = false;

    private Set<DynamicController> controllerCache = new HashSet<>();

    public SpringRequestMappingComponent(ApplicationContext applicationContext,
                                         SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;

        jsonMapper = springBootStartInfo.getJsonMapper();

    }

    @Override
    public void invokeScheduled(String className, String methodName, String param) {
        try {
            doInvokeScheduled(className, methodName, param);
        } catch (Exception e) {
            CoolRequestProjectLog.userExceptionLog(e);
        }
    }

    @Override
    public InvokeResponseModel invokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody) {
        try {
            return doInvokeController(reflexHttpRequestParamAdapterBody);
        } catch (Exception e) {
            CoolRequestProjectLog.userExceptionLog(e);
            return new ExceptionInvokeResponseModel(reflexHttpRequestParamAdapterBody.getId(), e);
        }
    }

    private void doInvokeScheduled(String className, String methodName, String param) throws Exception {
        CoolRequestProjectLog.log("调用定时器:" + className + "." + methodName);
        try {
            for (DynamicSpringScheduled scheduledInvokeBean : scheduledInvokeBeans) {
                if (className.equals(scheduledInvokeBean.getClassName()) &&
                        methodName.equals(scheduledInvokeBean.getMethodName())) {
                    ScheduledEndpoint scheduledEndpoint = scheduledInvokeBean.getAttachScheduledEndpoint();
                    LOGGER.info("invoke scheduled {}.{}", scheduledEndpoint.getMethod().getDeclaringClass().getName(),
                            scheduledEndpoint.getMethod().getName());
                    scheduledEndpoint.getMethod().setAccessible(true);
                    scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean());
                }
            }
        } catch (Exception e) {
            CoolRequestProjectLog.logWithDebug(e);
        }
    }

    private InvokeResponseModel doInvokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody) throws
            Exception {
        MockClassLoader mockClassLoader = MockClassLoader.newMockClassLoader();
        Class<?> aClass = mockClassLoader.loadClass("com.cool.request.components.spring.controller.Dispatcher");
        Object dispatcher = aClass.getDeclaredConstructor(ApplicationContext.class, SpringRequestMappingComponent.class)
                .newInstance(applicationContext, this);
        try {
            MethodType methodType = MethodType.methodType(InvokeResponseModel.class, ReflexHttpRequestParamAdapterBody.class, int.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(aClass, "invokeController", methodType);
            Object invoke = handle.invoke(dispatcher, reflexHttpRequestParamAdapterBody, getServerPort(applicationContext));
            if (invoke instanceof InvokeResponseModel) {
                return ((InvokeResponseModel) invoke);
            }
        } catch (Throwable ignored) {
        }
        throw new RuntimeException();
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

    private List<DynamicController> collectorRequestMapping() {
        isScanning = true;
        int serverPort = getServerPort(applicationContext);
        List<DynamicController> result = new ArrayList<>();
        try {
            Map<String, RequestMappingHandlerMapping> beansOfType = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
            String contextPath = getContextPath(applicationContext);
            for (RequestMappingHandlerMapping requestMappingHandlerMapping : beansOfType.values()) {
                Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
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
                        result.add(dynamicController);
                        controllerCache.add(dynamicController);
                    }
                }
            }
        } finally {
            isScanning = false;
        }
        return result;

    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {
        //发送项目启动
        try {
            springBootStartInfo.getCoolRequestPluginRMI().projectStartup(springBootStartInfo.getAvailableTcpPort(),
                    getServerPort(applicationContext));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.refresh(false);
    }

    public void parseSpringScheduled(Object bean) {
        try {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            if (AnnotationUtilsAdapter.isCandidateClass(targetClass, Arrays.asList(Scheduled.class, Schedules.class))) {
                Set<Method> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                        (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, Scheduled.class) || AnnotatedElementUtils.hasAnnotation(method, Schedules.class));


                if (!annotatedMethods.isEmpty()) {
                    annotatedMethods.forEach((method) -> {
                        DynamicSpringScheduled springScheduled = DynamicSpringScheduled.DynamicSpringScheduledBuilder.aDynamicSpringScheduled()
                                .withClassName(method.getDeclaringClass().getName())
                                .withMethodName(method.getName())
                                .withServerPort(springBootStartInfo.getAvailableTcpPort())
                                .build();

                        ScheduledEndpoint scheduledEndpoint = new ScheduledEndpoint(method, bean);
                        springScheduled.setAttachScheduledEndpoint(scheduledEndpoint);
                        scheduledInvokeBeans.add(springScheduled);

                    });
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            CoolRequestProjectLog.logWithDebug(ignored);
        }
    }

    private void doRefresh(boolean ignoreSize) {
        try {
            CoolRequestProjectLog.log("MVC推送数据");
            List<DynamicController> controllers = collectorRequestMapping();
            springBootStartInfo.getCoolRequestPluginRMI()
                    .loadController(controllers);

            CoolRequestProjectLog.log("准备解析定时器");
            for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
                parseSpringScheduled(applicationContext.getBean(beanDefinitionName));
            }
            CoolRequestProjectLog.log("定时器解析完成，共" + scheduledInvokeBeans.size() + "个");
            springBootStartInfo.getCoolRequestPluginRMI().loadScheduled(this.scheduledInvokeBeans);
        } catch (Exception e) {
            e.printStackTrace();
            CoolRequestProjectLog.log(e.getMessage());
        } finally {
            refreshing = false;
        }
    }

    private void refresh(boolean ignoreSize) {
        if (refreshing) return;
        refreshing = true;
        threadPoolExecutor.submit(() -> doRefresh(ignoreSize));
    }

}
