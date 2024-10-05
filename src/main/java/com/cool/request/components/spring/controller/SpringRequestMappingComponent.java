package com.cool.request.components.spring.controller;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.MockClassLoader;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.DynamicController;
import com.cool.request.components.http.ExceptionInvokeResponseModel;
import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.cool.request.utils.SpringUtils.getServerPort;


public class SpringRequestMappingComponent implements
        ComponentDataHandler,
        ControllerInvokeListener {
    private final Logger LOGGER = LoggerFactory.getLogger(SpringRequestMappingComponent.class);
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private final ApplicationContext applicationContext;
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
    public InvokeResponseModel invokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody) {
        try {
            LOGGER.info("invoke controller:" + reflexHttpRequestParamAdapterBody.getUrl());
            return doInvokeController(reflexHttpRequestParamAdapterBody);
        } catch (Throwable e) {
            CoolRequestProjectLog.userExceptionLog(e);
            return new ExceptionInvokeResponseModel(reflexHttpRequestParamAdapterBody.getId(), e);
        }
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
        List<DynamicController> dynamicControllers = new ArrayList<>();
        try {
            for (ControllerCollector controllerCollector : controllerCollectors) {
                try {
                    List<DynamicController> collect = controllerCollector.collect(applicationContext, springBootStartInfo);
                    if (collect != null) {
                        dynamicControllers.addAll(collect);
                    }
                } catch (Throwable ignored) {
                }
            }

        } catch (Exception e) {
            CoolRequestProjectLog.log(e.getMessage());
        } finally {
            try {
                springBootStartInfo.getCoolRequestPluginRMI()
                        .loadController(dynamicControllers);
            } catch (RemoteException ignored) {

            }
        }
        refreshing = false;
    }

    private void refresh(boolean ignoreSize) {
        if (refreshing) return;
        refreshing = true;
        threadPoolExecutor.submit(() -> doRefresh(ignoreSize));
    }

}
