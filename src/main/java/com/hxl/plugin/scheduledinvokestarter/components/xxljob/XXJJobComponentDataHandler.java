package com.hxl.plugin.scheduledinvokestarter.components.xxljob;

import com.hxl.plugin.scheduledinvokestarter.PluginCommunication;
import com.hxl.plugin.scheduledinvokestarter.ScheduledEndpoint;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import com.hxl.plugin.scheduledinvokestarter.model.XxlJobInvokeEndpoint;
import com.hxl.plugin.scheduledinvokestarter.model.XxlModel;
import com.hxl.plugin.scheduledinvokestarter.model.pack.XXLJobPackage;
import com.hxl.plugin.scheduledinvokestarter.utils.AnnotationUtilsAdapter;
import com.hxl.plugin.scheduledinvokestarter.utils.ApplicationHome;
import com.hxl.plugin.scheduledinvokestarter.utils.CoolRequestStarConfig;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

public class XXJJobComponentDataHandler implements ComponentDataHandler {
    private final Logger LOGGER = Logger.getLogger(XXJJobComponentDataHandler.class.getName());
    private JsonMapper jsonMapper;
    private final Map<String, ScheduledEndpoint> xxlJobEndpointMap = new HashMap<>();
    private final List<XxlJobInvokeEndpoint> xxlJobInvokeBeans = new ArrayList<>();

    private SpringBootStartInfo springBootStartInfo;

    public XXJJobComponentDataHandler(JsonMapper jsonMapper, ApplicationContext applicationContext,
                                      SpringBootStartInfo springBootStartInfo) {
        this.jsonMapper = jsonMapper;
        this.springBootStartInfo = springBootStartInfo;
    }

    @Override
    public void publishData(ApplicationContext applicationContext) {
        new Thread(() -> {
            xxlJobInvokeBeans.clear();
            xxlJobEndpointMap.clear();
            for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
                postProcessBeforeInitialization(applicationContext.getBean(beanDefinitionName), beanDefinitionName);
            }
            if (CoolRequestStarConfig.isDebug()) {
                LOGGER.info("xxl-job count=" + xxlJobEndpointMap.size());
            }
            XxlModel xxlModel = new XxlModel();
            xxlModel.setServerPort(springBootStartInfo.getAvailableTcpPort());
            xxlModel.setXxlJobInvokeEndpoint(xxlJobInvokeBeans);
            PluginCommunication.send(new XXLJobPackage(xxlModel));
        }).start();
    }

    public void postProcessBeforeInitialization(Object bean, String beanName) {
        try {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            boolean candidateClass = AnnotationUtilsAdapter.isCandidateClass(targetClass, Collections.singletonList(XxlJob.class));
            if (candidateClass) {
                Set<Method> methods = MethodIntrospector.selectMethods(targetClass, (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, XxlJob.class));

                if (!methods.isEmpty()) {
                    methods.forEach((method) -> {
                        XxlJobInvokeEndpoint xxlJobInvokeEndpoint =
                                XxlJobInvokeEndpoint.XxlJobInvokeEndpointBuilder.aXxlJobInvokeEndpoint()
                                        .withClassName(method.getDeclaringClass().getName())
                                        .withMethodName(method.getName())
                                        .build();
                        xxlJobInvokeEndpoint.setSpringInnerId(generatorId(method));
                        ScheduledEndpoint scheduledEndpoint = new ScheduledEndpoint(method, bean);
                        xxlJobEndpointMap.put(xxlJobInvokeEndpoint.getSpringInnerId(), scheduledEndpoint);
                        xxlJobInvokeBeans.add(xxlJobInvokeEndpoint);


                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void messageData(String msg) {
        try {
            Map<String, Object> taskMap = jsonMapper.toMap(msg);

            if (taskMap.getOrDefault("type", "").equals("scheduled")) {
                invokeScheduled(taskMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeScheduled(Map<String, Object> taskMap) {
        String id = taskMap.getOrDefault("id", "").toString();
        try {
            if (xxlJobEndpointMap.containsKey(id)) {
                ScheduledEndpoint scheduledEndpoint = xxlJobEndpointMap.get(id);
                scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generatorId(Method method) {
        String name = method.getName();
        String className = method.getDeclaringClass().getSimpleName();
        String project = new ApplicationHome().getDir().toString();
        return DigestUtils.md5DigestAsHex(("scheduled" + name + className + CoolRequestStarConfig.getProjectName(project) + method.getDeclaringClass()).getBytes());
    }

}