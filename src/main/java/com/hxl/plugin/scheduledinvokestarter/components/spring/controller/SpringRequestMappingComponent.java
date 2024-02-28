package com.hxl.plugin.scheduledinvokestarter.components.spring.controller;

import com.hxl.plugin.scheduledinvokestarter.*;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapperFactory;
import com.hxl.plugin.scheduledinvokestarter.model.*;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ClearCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ProjectStartupCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.RequestMappingCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ScheduledCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.utils.AnnotationUtilsAdapter;
import com.hxl.plugin.scheduledinvokestarter.utils.ApplicationHome;
import com.hxl.plugin.scheduledinvokestarter.utils.SystemUtils;
import com.hxl.plugin.scheduledinvokestarter.utils.VersionUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.util.DigestUtils;
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
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hxl.plugin.scheduledinvokestarter.utils.SpringUtils.getContextPath;
import static com.hxl.plugin.scheduledinvokestarter.utils.SpringUtils.getServerPort;


public class SpringRequestMappingComponent implements ComponentDataHandler {
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final String EMPTY_STRING = "";
    private final ApplicationContext applicationContext;
    private final Map<String, ControllerEndpoint> handlerMethodMaps = new HashMap<>();
    private final Map<String, ScheduledEndpoint> scheduledEndpointMap = new HashMap<>();
    private final List<SpringScheduledSpringInvokeEndpoint> scheduledInvokeBeans = new ArrayList<>();
    private final SpringBootStartInfo springBootStartInfo;

    public Map<String, ControllerEndpoint> getHandlerMethodMaps() {
        return handlerMethodMaps;
    }

    public Map<String, ScheduledEndpoint> getScheduledEndpointMap() {
        return scheduledEndpointMap;
    }

    public static JsonMapper jsonMapper;
    private boolean refreshing = false;

    public SpringRequestMappingComponent(ApplicationContext applicationContext,
                                         SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;

        String json = applicationContext.getEnvironment().getProperty("cool.request.plugin.json");
        jsonMapper = JsonMapperFactory.getJsonMapper(json);
        if (jsonMapper == null) {
            System.err.println("Unable to find JSON tool");
        }
    }

    @Override
    public void messageData(String msg) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> taskMap = jsonMapper.toMap(msg);

                    if (taskMap.getOrDefault("type", "").equals("refresh")) {
                        SpringRequestMappingComponent.this.refresh();
                        return;
                    }
                    if (taskMap.getOrDefault("type", "").equals("controller")) {
                        invokeController(msg);
                    }
                    if (taskMap.getOrDefault("type", "").equals("scheduled")) {
                        invokeScheduled(taskMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void invokeScheduled(Map<String, Object> msg) throws Exception {
        MockClassLoader mockClassLoader = MockClassLoader.newMockClassLoader();
        Class<?> aClass = mockClassLoader.loadClass("com.hxl.plugin.scheduledinvokestarter.components.spring.controller.Dispatcher");
        Object o = aClass.getDeclaredConstructor(ApplicationContext.class, SpringRequestMappingComponent.class).newInstance(applicationContext, this);
        try {
            MethodType methodType = MethodType.methodType(void.class, Map.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(aClass, "invokeSchedule", methodType);
            handle.invoke(o, msg);
        } catch (Throwable e) {
        }
    }

    private void invokeController(String msg) throws Exception {
        MockClassLoader mockClassLoader = MockClassLoader.newMockClassLoader();
        Class<?> aClass = mockClassLoader.loadClass("com.hxl.plugin.scheduledinvokestarter.components.spring.controller.Dispatcher");
        Object o = aClass.getDeclaredConstructor(ApplicationContext.class, SpringRequestMappingComponent.class).newInstance(applicationContext, this);
        try {
            ControllerRequestData controllerRequestData = jsonMapper.toBean(msg, ControllerRequestData.class);
            MethodType methodType = MethodType.methodType(void.class, ControllerRequestData.class, int.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(aClass, "invokeController", methodType);
            handle.invoke(o, controllerRequestData, getServerPort(applicationContext));
        } catch (Throwable e) {
        }
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

        }
        return Arrays.stream(handlerMethod.getMethod().getParameterTypes()).map(aClass -> aClass.getSimpleName()).collect(Collectors.toList());
    }

    private Set<com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller> collectorRequestMapping() {
        Map<String, RequestMappingHandlerMapping> beansOfType = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        String contextPath = getContextPath(applicationContext);
        int serverPort = getServerPort(applicationContext);
        Set<com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller> result = new HashSet<>();
        for (RequestMappingHandlerMapping requestMappingHandlerMapping : beansOfType.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {
                HandlerMethod handlerMethod = handlerMethods.get(requestMappingInfo);
                for (String url : getUrlPattern(requestMappingInfo)) {
                    RequestMethod requestMethod = requestMappingInfo.getMethodsCondition().getMethods().stream().findFirst().orElse(RequestMethod.GET);
                    com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller controller =
                            com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller.ControllerBuilder
                                    .aController()
                                    .withContextPath(contextPath)
                                    .withHttpMethod(requestMethod.name())
                                    .withMethodName(handlerMethod.getMethod().getName())
                                    .withUrl(url)
                                    .withServerPort(serverPort)
                                    .withSimpleClassName(handlerMethod.getBeanType().getName())
                                    .build(new com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller());
                    controller.setParamClassList(getParamClassList(handlerMethod));

                    String id = generatorId(controller);
                    controller.setSpringInnerId(id);
                    handlerMethodMaps.put(id, new ControllerEndpoint(requestMappingInfo, handlerMethod));
                    result.add(controller);
                }
            }
        }
        return result;
    }

    @Override
    public void publishData(ApplicationContext applicationContext) {
        ProjectStartupModel projectStartupModel = new ProjectStartupModel(springBootStartInfo.getAvailableTcpPort());
        projectStartupModel.setProjectPort(getServerPort(applicationContext));
        PluginCommunication.send(new ProjectStartupCommunicationPackage(projectStartupModel));
        this.refresh();
    }

    private ScheduledAnnotationBeanPostProcessor getScheduledAnnotationBeanPostProcessorBean() {
        try {
            return this.applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        } catch (Exception ignored) {
        }
        return null;
    }

    public void postProcessBeforeInitialization(Object bean, String beanName) {
        try {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            if (AnnotationUtilsAdapter.isCandidateClass(targetClass, Arrays.asList(Scheduled.class, Schedules.class))) {
                Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                        (MethodIntrospector.MetadataLookup<Set<Scheduled>>) method -> {
                            Set<Scheduled> scheduledAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
                                    method, Scheduled.class, Schedules.class);
                            return (!scheduledAnnotations.isEmpty() ? scheduledAnnotations : null);
                        });
                if (!annotatedMethods.isEmpty()) {
                    annotatedMethods.forEach((method, s) -> {
                        SpringScheduledSpringInvokeEndpoint scheduledInvokeBean =
                                SpringScheduledSpringInvokeEndpoint.ScheduledInvokeBeanBuilder.aScheduledInvokeBean()
                                        .withClassName(method.getDeclaringClass().getName())
                                        .withMethodName(method.getName())
                                        .build();
                        scheduledInvokeBean.setSpringInnerId(generatorId(method));
                        ScheduledEndpoint scheduledEndpoint = new ScheduledEndpoint(method, bean);
                        scheduledEndpointMap.put(scheduledInvokeBean.getSpringInnerId(), scheduledEndpoint);
                        scheduledInvokeBeans.add(scheduledInvokeBean);

                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void doRefresh() {
        try {
            Set<com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller> controllers = collectorRequestMapping();
            if (SystemUtils.isDebug()) {
                System.out.println(jsonMapper.toJSONString(controllers));
            }
            PluginCommunication.send(new ClearCommunicationPackage(new ClearModel()));//插件目前没有响应

            //已进度条得方法发送到插件
            for (Controller controller : controllers) {
                RequestMappingModel requestMappingModel = new RequestMappingModel();
                requestMappingModel.setController(controller);
                requestMappingModel.setTotal(controllers.size());
                requestMappingModel.setPluginPort(springBootStartInfo.getAvailableTcpPort());
                requestMappingModel.setServerPort(getServerPort(applicationContext));
                RequestMappingCommunicationPackage requestMappingCommunicationPackage = new RequestMappingCommunicationPackage(requestMappingModel);
                PluginCommunication.send(requestMappingCommunicationPackage);
            }

            for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
                postProcessBeforeInitialization(applicationContext.getBean(beanDefinitionName), beanDefinitionName);
            }
            PluginCommunication.send(new ScheduledCommunicationPackage(new ScheduledModel(scheduledInvokeBeans, this.springBootStartInfo.getAvailableTcpPort())));
        } catch (Exception e) {
            if (SystemUtils.isDebug()) {
                e.printStackTrace();
            }
        } finally {
            refreshing = false;
        }
    }

    private void refresh() {
        if (refreshing) return;
        refreshing = true;
        threadPoolExecutor.submit(this::doRefresh);
    }

    private String generatorId(Controller controller) {
        String project = new ApplicationHome().getDir().toString();
        String id = new StringBuilder()
                .append(SystemUtils.getProjectName(project))
                .append(controller.getServerPort())
                .append(controller.getSimpleClassName())
                .append(controller.getMethodName())
                .append(controller.getHttpMethod())
                .append(controller.getUrl())
                .append(controller.getParamClassList())
                .toString();
        return DigestUtils.md5DigestAsHex(id.getBytes());
    }

    private String generatorId(Method method) {
        String name = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String project = new ApplicationHome().getDir().toString();
        return DigestUtils.md5DigestAsHex(("scheduled" + name + className + SystemUtils.getProjectName(project) + method.getDeclaringClass()).getBytes());
    }

    private String generatorId() {
        return UUID.randomUUID().toString();
    }

}
