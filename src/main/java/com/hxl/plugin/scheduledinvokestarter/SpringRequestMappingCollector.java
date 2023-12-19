package com.hxl.plugin.scheduledinvokestarter;

import com.hxl.plugin.scheduledinvokestarter.json.JsonException;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapperFactory;
import com.hxl.plugin.scheduledinvokestarter.model.*;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ClearCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ProjectStartupCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.RequestMappingCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.model.pack.ScheduledCommunicationPackage;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SpringRequestMappingCollector implements CommandLineRunner,
        ApplicationContextAware, PluginCommunication.MessageCallback {
    private static final String EMPTY_STRING = "";
    private ApplicationContext applicationContext;
    private final Map<String, ControllerEndpoint> handlerMethodMaps = new HashMap<>();
    private final Map<String, ScheduledEndpoint> scheduledEndpointMap = new HashMap<>();
    private final List<SpringScheduledSpringInvokeEndpoint> scheduledInvokeBeans = new ArrayList<>();
    private final PluginCommunication pluginCommunication = new PluginCommunication(this);
    private int availableTcpPort;

    public Map<String, ControllerEndpoint> getHandlerMethodMaps() {
        return handlerMethodMaps;
    }

    public Map<String, ScheduledEndpoint> getScheduledEndpointMap() {
        return scheduledEndpointMap;
    }

    public static JsonMapper jsonMapper;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    @Override
    public void pluginMessage(String msg) {
     threadPoolExecutor.submit(new Runnable() {
         @Override
         public void run() {
             try {
                 Map<String, Object> taskMap = jsonMapper.toMap(msg);
                 if (SystemUtils.isDebug()){
                     System.out.println(taskMap);
                 }
                 Dispatcher dispatcher = new Dispatcher(SpringRequestMappingCollector.this.applicationContext,
                         SpringRequestMappingCollector.this);

                 if (taskMap.getOrDefault("type", "").equals("controller")) dispatcher.invokeController(taskMap);
                 if (taskMap.getOrDefault("type", "").equals("scheduled")) dispatcher.invokeSchedule(taskMap);
                 if (taskMap.getOrDefault("type", "").equals("refresh")) SpringRequestMappingCollector.this.refresh();

             } catch (JsonException e) {
                 e.printStackTrace();
             }
         }
     });
    }

    private boolean hasMethod(Class<?> targetClass, String methodName, Class<?> retClass, Class<?>... ptypes) {
        try {
            MethodHandles.lookup().findVirtual(targetClass, methodName, MethodType.methodType(retClass, ptypes));
            return true;
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        return false;

    }

    private String getUrlPattern(RequestMappingInfo requestMappingInfo) {
        try {
            if (hasMethod(requestMappingInfo.getClass(), "getPatternValues", Set.class)) {
                return requestMappingInfo.getPatternValues().stream().findFirst().orElseGet(() -> EMPTY_STRING);
            }
            PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
            if (patternsCondition != null) {
                return patternsCondition.getPatterns().stream().findFirst().orElseGet(() -> EMPTY_STRING);
            }
            PathPatternsRequestCondition pathPatternsCondition = requestMappingInfo.getPathPatternsCondition();
            if (pathPatternsCondition != null) {
                Optional<PathPattern> first = pathPatternsCondition.getPatterns().stream().findFirst();
                if (first.isPresent()) return first.get().getPatternString();
            }
        } catch (Exception ignored) {
        }
        return EMPTY_STRING;
    }

    private Set<SpringMvcRequestMappingSpringInvokeEndpoint> collectorRequestMapping() {
        Map<String, RequestMappingHandlerMapping> beansOfType = applicationContext.getBeansOfType(RequestMappingHandlerMapping.class);
        Set<SpringMvcRequestMappingSpringInvokeEndpoint> springMvcRequestMappingInvokeBeans = new HashSet<>();
        for (RequestMappingHandlerMapping requestMappingHandlerMapping : beansOfType.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {
                HandlerMethod handlerMethod = handlerMethods.get(requestMappingInfo);
                String id = generatorId(requestMappingInfo, handlerMethod);
                handlerMethodMaps.put(id, new ControllerEndpoint(requestMappingInfo, handlerMethod));
                String url = getUrlPattern(requestMappingInfo);
                RequestMethod requestMethod = requestMappingInfo.getMethodsCondition().getMethods().stream().findFirst().orElse(RequestMethod.GET);
                springMvcRequestMappingInvokeBeans.add(SpringMvcRequestMappingSpringInvokeEndpoint.RequestMappingInvokeBeanBuilder.aRequestMappingInvokeBean()
                        .withId(id)
                        .withHttpMethod(requestMethod.name())
                        .withMethodName(handlerMethod.getMethod().getName())
                        .withUrl(url)
                        .withSimpleClassName(handlerMethod.getBeanType().getName())
                        .build());

            }
        }
        return springMvcRequestMappingInvokeBeans;
    }

//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        try {
//            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
//            if (AnnotationUtils.isCandidateClass(targetClass, Arrays.asList(Scheduled.class, Schedules.class))) {
//                Map<Method, Set<Scheduled>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
//                        (MethodIntrospector.MetadataLookup<Set<Scheduled>>) method -> {
//                            Set<Scheduled> scheduledAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
//                                    method, Scheduled.class, Schedules.class);
//                            return (!scheduledAnnotations.isEmpty() ? scheduledAnnotations : null);
//                        });
//                if (!annotatedMethods.isEmpty()) {
//                    annotatedMethods.forEach((method, s) -> {
//                        SpringScheduledSpringInvokeEndpoint scheduledInvokeBean = SpringScheduledSpringInvokeEndpoint.ScheduledInvokeBeanBuilder.aScheduledInvokeBean()
//                                .withId(generatorId(method))
//                                .withClassName(method.getDeclaringClass().getName())
//                                .withMethodName(method.getName())
//                                .build();
//                        ScheduledEndpoint scheduledEndpoint = new ScheduledEndpoint(method, bean);
//                        scheduledEndpointMap.put(scheduledInvokeBean.getId(), scheduledEndpoint);
//                        scheduledInvokeBeans.add(scheduledInvokeBean);
//                    });
//                }
//            }
//        } catch (Exception ignored) {
//        }
//        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
//    }


    @Override
    public void run(String... args) throws Exception {
        String json = applicationContext.getEnvironment().getProperty("cool.request.plugin.json");
        jsonMapper = JsonMapperFactory.getJsonMapper(json);
        if (jsonMapper == null) {
            System.err.println("Unable to find JSON tool");
            return;
        }
        availableTcpPort = SocketUtils.findAvailableTcpPort();
        pluginCommunication.startServer(availableTcpPort);
        ProjectStartupModel projectStartupModel = new ProjectStartupModel(availableTcpPort);
        projectStartupModel.setProjectPort(getServerPort());
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

    private void refresh() {
        Set<SpringMvcRequestMappingSpringInvokeEndpoint> springMvcRequestMappingInvokeBeans = collectorRequestMapping();

        PluginCommunication.send(new ClearCommunicationPackage(new ClearModel()));
        ArrayList<SpringMvcRequestMappingSpringInvokeEndpoint> requestMapping = new ArrayList<>(springMvcRequestMappingInvokeBeans);
        for (int i = 0; i < requestMapping.size(); i++) {
            RequestMappingModel requestMappingModel = RequestMappingModel.RequestMappingModelBuilder.aRequestMappingModel()
                    .withPort(availableTcpPort)
                    .withContextPath(getContextPath())
                    .withServerPort(getServerPort())
                    .withCurrent(i + 1)
                    .withTotal(springMvcRequestMappingInvokeBeans.size())
                    .withRequestMappingInvokeBean(requestMapping.get(i))
                    .build();
            RequestMappingCommunicationPackage requestMappingCommunicationPackage = new RequestMappingCommunicationPackage(requestMappingModel);
            PluginCommunication.send(requestMappingCommunicationPackage);
        }
        ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessorBean = getScheduledAnnotationBeanPostProcessorBean();
        if (scheduledAnnotationBeanPostProcessorBean != null) {
            Set<ScheduledTask> scheduledTasks = scheduledAnnotationBeanPostProcessorBean.getScheduledTasks();
            for (ScheduledTask scheduledTask : scheduledTasks) {
                Runnable runnable = scheduledTask.getTask().getRunnable();
                if (runnable instanceof ScheduledMethodRunnable) {
                    Method method = ((ScheduledMethodRunnable) runnable).getMethod();
                    SpringScheduledSpringInvokeEndpoint scheduledInvokeBean = SpringScheduledSpringInvokeEndpoint.ScheduledInvokeBeanBuilder.aScheduledInvokeBean()
                            .withId(generatorId(method))
                            .withClassName(method.getDeclaringClass().getName())
                            .withMethodName(method.getName())
                            .build();
                    ScheduledEndpoint scheduledEndpoint = new ScheduledEndpoint(method, ((ScheduledMethodRunnable) runnable).getTarget());
                    scheduledEndpointMap.put(scheduledInvokeBean.getId(), scheduledEndpoint);
                    scheduledInvokeBeans.add(scheduledInvokeBean);
                }
            }
        }
        PluginCommunication.send(new ScheduledCommunicationPackage(new ScheduledModel(scheduledInvokeBeans, availableTcpPort)));
    }

    private int getServerPort() {
        String port = applicationContext.getEnvironment().getProperty("server.port");
        if (port == null || "0".equalsIgnoreCase(port)) return 8080;
        return Integer.parseInt(port);
    }

    private String getContextPath() {
        String contextPath = applicationContext.getEnvironment().getProperty("server.servlet.context-path");
        if (contextPath == null) return "";
        return contextPath;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private String generatorId(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        String name = handlerMethod.getMethod().getDeclaringClass().getName() + "." + handlerMethod.getMethod().getName();
        String project = new ApplicationHome().getDir().toString();
        String patternsString = getUrlPattern(requestMappingInfo);
        return DigestUtils.md5DigestAsHex((name + project + patternsString).getBytes());
    }

    private String generatorId(Method method) {
        String name = method.getName();
        String project = new ApplicationHome().getDir().toString();
        return DigestUtils.md5DigestAsHex(("scheduled" + name + project + method.getDeclaringClass()).getBytes());
    }

    private String generatorId() {
        return UUID.randomUUID().toString();
    }

}
