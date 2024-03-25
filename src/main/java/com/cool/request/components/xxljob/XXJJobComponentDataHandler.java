package com.cool.request.components.xxljob;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.ScheduledEndpoint;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.scheduled.DynamicXxlJobScheduled;
import com.cool.request.components.scheduled.ScheduledListener;
import com.cool.request.utils.AnnotationUtilsAdapter;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class XXJJobComponentDataHandler implements ComponentDataHandler, ScheduledListener {
    private final Logger LOGGER = Logger.getLogger(XXJJobComponentDataHandler.class.getName());
    private final Map<String, ScheduledEndpoint> xxlJobEndpointMap = new HashMap<>();
    private final List<DynamicXxlJobScheduled> xxlJobInvokeBeans = new ArrayList<>();

    private final SpringBootStartInfo springBootStartInfo;
    private final ApplicationContext applicationContext;

    public XXJJobComponentDataHandler(ApplicationContext applicationContext,
                                      SpringBootStartInfo springBootStartInfo) {
        this.springBootStartInfo = springBootStartInfo;
        this.applicationContext = applicationContext;
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {
        refresh();
    }

    private void doRefresh() {
        xxlJobInvokeBeans.clear();
        xxlJobEndpointMap.clear();
        for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
            parseXXLJob(applicationContext.getBean(beanDefinitionName));
        }
        CoolRequestProjectLog.log("xxl-job count=" + xxlJobEndpointMap.size());
        try {
            springBootStartInfo.getCoolRequestPluginRMI().loadXXLScheduled(xxlJobInvokeBeans);
        } catch (RemoteException ignored) {
        }
    }

    private void refresh() {
        new Thread(this::doRefresh).start();
    }

    @Override
    public void invokeScheduled(String className, String methodName, String param) {
        doInvokeScheduled(className, methodName, param);
    }

    public void parseXXLJob(Object bean) {
        try {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            boolean candidateClass = AnnotationUtilsAdapter.isCandidateClass(targetClass, Collections.singletonList(XxlJob.class));
            if (candidateClass) {
                Set<Method> methods = MethodIntrospector.selectMethods(targetClass, (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, XxlJob.class));

                if (!methods.isEmpty()) {
                    methods.forEach((method) -> {
                        DynamicXxlJobScheduled dynamicXxlJobScheduled = DynamicXxlJobScheduled.DynamicXxlJobScheduledBuilder
                                .aDynamicXxlJobScheduled()
                                .withClassName(method.getDeclaringClass().getName())
                                .withMethodName(method.getName())
                                .withServerPort(springBootStartInfo.getAvailableTcpPort())
                                .build();
                        dynamicXxlJobScheduled.setAttachScheduledEndpoint(new ScheduledEndpoint(method, bean));
                        xxlJobInvokeBeans.add(dynamicXxlJobScheduled);
                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void doInvokeScheduled(String className, String methodName, String param) {
        try {
            CoolRequestProjectLog.log("调用xxl-job" + className + "." + methodName);
            for (DynamicXxlJobScheduled xxlJobInvokeBean : xxlJobInvokeBeans) {
                if (className.equals(xxlJobInvokeBean.getClassName()) &&
                        methodName.equals(xxlJobInvokeBean.getMethodName())) {
                    ScheduledEndpoint scheduledEndpoint = xxlJobInvokeBean.getAttachScheduledEndpoint();
                    XxlJobContext.setXxlJobContext(new XxlJobContext(0, param, null, 0, 0));
                    int parameterCount = scheduledEndpoint.getMethod().getParameterCount();
                    if (parameterCount == 0) {
                        scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean());
                    }
                    if (parameterCount == 1 && scheduledEndpoint.getMethod().getParameters()[0].getType() == String.class) {
                        scheduledEndpoint.getMethod().invoke(scheduledEndpoint.getBean(), param);
                    }
                }
            }

        } catch (Exception e) {
            CoolRequestProjectLog.userExceptionLog(e);
        }
    }
}
