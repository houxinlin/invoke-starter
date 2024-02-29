package com.hxl.plugin.scheduledinvokestarter.components.spring.gateway;


import com.hxl.plugin.scheduledinvokestarter.PluginCommunication;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.model.GatewayModel;
import com.hxl.plugin.scheduledinvokestarter.model.pack.SpringGatewayCommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.utils.SpringUtils;
import com.hxl.plugin.scheduledinvokestarter.utils.CoolRequestStarConfig;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringGatewayComponent implements ComponentDataHandler {
    private ApplicationContext applicationContext;
    private SpringBootStartInfo springBootStartInfo;
    private static final ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(2);

    public SpringGatewayComponent(ApplicationContext applicationContext,
                                  SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
    }

    @Override
    public void publishData(ApplicationContext applicationContext) {
        scheduledThreadPool.scheduleAtFixedRate(() -> doPush(), 0, 10, TimeUnit.SECONDS);
    }

    private void doPush() {
        RouteLocator bean = applicationContext.getBean(RouteLocator.class);
        List<GatewayModel.Gateway> gatewayModels = new ArrayList<>();
        List<Route> block = bean.getRoutes().collectList().block();
        if (block == null) return;
        int serverPort = SpringUtils.getServerPort(applicationContext);
        for (Route route : block) {
            AsyncPredicate<ServerWebExchange> predicate = route.getPredicate();
            if (predicate instanceof AsyncPredicate.DefaultAsyncPredicate) {
                Field delegate = ReflectionUtils.findField(AsyncPredicate.DefaultAsyncPredicate.class, "delegate");
                if (delegate == null) break;
                ReflectionUtils.makeAccessible(delegate);
                Object delegateValue = ReflectionUtils.getField(delegate, predicate);
                if (delegateValue == null) return;
                GatewayModel.Gateway gatewayModel = new GatewayModel.Gateway();
                gatewayModel.setRouteId(route.getId());
                gatewayModel.setId(getId(serverPort, route.getId()));
                gatewayModel.setUrl(route.getUri().toString());
                gatewayModel.setPrefix(getPredicate(delegateValue.toString()));
                gatewayModels.add(gatewayModel);
            }
        }
        GatewayModel gatewayModel = new GatewayModel();
        gatewayModel.setType("spring_gateway");
        gatewayModel.setGateways(gatewayModels);
        gatewayModel.setContext(SpringUtils.getContextPath(applicationContext));
        gatewayModel.setPort(serverPort);
        PluginCommunication.send(new SpringGatewayCommunicationPackage(gatewayModel));
    }

    @Override
    public void messageData(String msg) {

    }


    private String getId(int port, String routeId) {
        String project = new ApplicationHome().getDir().toString();
        return DigestUtils.md5DigestAsHex(("spring_gateway" + routeId + CoolRequestStarConfig.getProjectName(project) + port).getBytes());
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
