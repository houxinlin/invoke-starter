package com.cool.request.components.spring.gateway;


import com.cool.request.CoolRequestProjectLog;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.utils.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringGatewayComponent implements ComponentDataHandler {
    private final ApplicationContext applicationContext;
    private final SpringBootStartInfo springBootStartInfo;

    public SpringGatewayComponent(ApplicationContext applicationContext,
                                  SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {
        new Thread(() -> doPush()).start();
    }

    private void doPush() throws BeansException {
        try {
            RouteLocator bean = applicationContext.getBean(RouteLocator.class);
            List<Route> block = bean.getRoutes().collectList().block();
            if (block == null) return;
            int serverPort = SpringUtils.getServerPort(applicationContext);
            String contextPath = SpringUtils.getContextPath(applicationContext);
            for (Route route : block) {
                AsyncPredicate<ServerWebExchange> predicate = route.getPredicate();
                if (predicate instanceof AsyncPredicate.DefaultAsyncPredicate) {
                    Field delegate = ReflectionUtils.findField(AsyncPredicate.DefaultAsyncPredicate.class, "delegate");
                    if (delegate == null) break;
                    ReflectionUtils.makeAccessible(delegate);
                    Object delegateValue = ReflectionUtils.getField(delegate, predicate);
                    if (delegateValue == null) return;
                    try {
                        springBootStartInfo.getCoolRequestPluginRMI()
                                .loadGateway(contextPath, serverPort, getPredicate(delegateValue.toString()), route.getId());
                    } catch (RemoteException ignored) {

                    }
                }
            }
        } catch (Exception e) {
            CoolRequestProjectLog.logWithDebug(e);
        }

    }

    private String getPredicate(String src) {
        String regex = "\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        if (matcher.find()) {
            String content = matcher.group(1);
            String[] result = content.split(",\\s*");
            for (String value : result) {
                return value;
            }
        }
        return null;
    }
}
