package com.cool.request.components.spring.controller;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.MockClassLoader;
import com.cool.request.ScheduledEndpoint;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.DynamicController;
import com.cool.request.components.http.ExceptionInvokeResponseModel;
import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.components.scheduled.DynamicSpringScheduled;
import com.cool.request.components.scheduled.ScheduledListener;
import com.cool.request.json.JsonMapper;
import com.cool.request.utils.AnnotationUtilsAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    public static JsonMapper jsonMapper;
    private boolean refreshing = false;

    private List<ControllerCollector> controllerCollectors = new ArrayList<>();

    public SpringRequestMappingComponent(ApplicationContext applicationContext,
                                         SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;

        controllerCollectors.add(new RequestMappingCollector());
        controllerCollectors.add(new WebMvcEndpointHandlerMappingCollector());
        jsonMapper = springBootStartInfo.getJsonMapper();

    }

    @Override
    public boolean invokeScheduled(String className, String methodName, String param) {
        try {
            return doInvokeScheduled(className, methodName, param);
        } catch (Exception e) {
            CoolRequestProjectLog.userExceptionLog(e);
        }
        return false;
    }

    @Override
    public InvokeResponseModel invokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody) {
        try {
            return doInvokeController(reflexHttpRequestParamAdapterBody);
        } catch (Throwable e) {
            CoolRequestProjectLog.userExceptionLog(e);
            return new ExceptionInvokeResponseModel(reflexHttpRequestParamAdapterBody.getId(), e);
        }
    }

    private boolean doInvokeScheduled(String className, String methodName, String param) throws Exception {
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
                    return true;
                }
            }
        } catch (Exception e) {
            CoolRequestProjectLog.logWithDebug(e);
        }
        return false;
    }

    private InvokeResponseModel doInvokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody) throws
            Throwable {
        MockClassLoader mockClassLoader = MockClassLoader.newMockClassLoader();
        Class<?> aClass = mockClassLoader.loadClass("com.cool.request.components.spring.controller.Dispatcher");
        Object dispatcher = aClass.getDeclaredConstructor(ApplicationContext.class).newInstance(applicationContext);
        MethodType methodType = MethodType.methodType(InvokeResponseModel.class, ReflexHttpRequestParamAdapterBody.class, int.class);
        MethodHandle handle = MethodHandles.lookup().findVirtual(aClass, "invokeController", methodType);
        Object invoke = handle.invoke(dispatcher, reflexHttpRequestParamAdapterBody, getServerPort(applicationContext));
        if (invoke == null) throw new IllegalArgumentException("invokeController invoke null");

        if (invoke instanceof InvokeResponseModel) {
            return ((InvokeResponseModel) invoke);
        }
        throw new IllegalArgumentException("arg error");
    }


    @Override
    public void componentInit(ApplicationContext applicationContext) {
        this.refresh(false);
    }

    private void doRefresh(boolean ignoreSize) {
        CoolRequestProjectLog.log("MVC推送数据");
        try {
            List<DynamicController> dynamicControllers = new ArrayList<>();
            for (ControllerCollector controllerCollector : controllerCollectors) {
                try {
                    List<DynamicController> collect = controllerCollector.collect(applicationContext, springBootStartInfo);
                    if (collect != null) {
                        dynamicControllers.addAll(collect);
                    }
                } catch (Exception ignored) {
                }
            }
            springBootStartInfo.getCoolRequestPluginRMI()
                    .loadController(dynamicControllers);
        } catch (Exception e) {
            CoolRequestProjectLog.log(e.getMessage());
        }

        try {
            CoolRequestProjectLog.log("准备解析定时器");
            Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
            for (Object bean : beans.values()) {
                parseSpringScheduled(bean);
            }
            CoolRequestProjectLog.log("定时器解析完成，共" + scheduledInvokeBeans.size() + "个");
            springBootStartInfo.getCoolRequestPluginRMI().loadScheduled(this.scheduledInvokeBeans);
        } catch (Exception e) {
            CoolRequestProjectLog.log(e.getMessage());
        }
        refreshing = false;
    }

    public void parseSpringScheduled(Object bean) {
        try {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            if (AnnotationUtilsAdapter.isCandidateClass(targetClass, Arrays.asList(Scheduled.class, Schedules.class))) {
                Set<Method> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                        (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, Scheduled.class)
                                || AnnotatedElementUtils.hasAnnotation(method, Schedules.class));

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
            CoolRequestProjectLog.logWithDebug(ignored);
        }
    }

    private void refresh(boolean ignoreSize) {
        if (refreshing) return;
        refreshing = true;
        threadPoolExecutor.submit(() -> doRefresh(ignoreSize));
    }

}
