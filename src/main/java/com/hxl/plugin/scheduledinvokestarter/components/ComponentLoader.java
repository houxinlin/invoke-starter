package com.hxl.plugin.scheduledinvokestarter.components;

import com.hxl.plugin.scheduledinvokestarter.PluginCommunication;
import com.hxl.plugin.scheduledinvokestarter.components.spring.controller.EnabledSpringMvcRequestMapping;
import com.hxl.plugin.scheduledinvokestarter.components.spring.gateway.EnabledSpringGateway;
import com.hxl.plugin.scheduledinvokestarter.components.xxljob.XxlJobComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapperFactory;
import com.hxl.plugin.scheduledinvokestarter.utils.SocketUtils;
import com.hxl.plugin.scheduledinvokestarter.utils.CoolRequestStarConfig;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

/**
 * 组件数据加载器
 */
public class ComponentLoader implements CommandLineRunner, ApplicationContextAware,
        PluginCommunication.MessageCallback {
    private List<ComponentSupport> componentLoaders = new ArrayList<>();
    private List<ComponentDataHandler> componentDataHandlers = new ArrayList<>();
    private ApplicationContext applicationContext;
    private final PluginCommunication pluginCommunication = new PluginCommunication(this);

    private int availableTcpPort;

    public ComponentLoader() {
        componentLoaders.add(new EnabledSpringMvcRequestMapping());
        componentLoaders.add(new EnabledSpringGateway());
        componentLoaders.add(new XxlJobComponentSupport());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void pluginMessage(String msg) {
        if (CoolRequestStarConfig.isDebug()) {
            System.out.println(msg);
        }
        for (ComponentDataHandler componentDataHandler : componentDataHandlers) {
            if (componentDataHandler != null) {
                componentDataHandler.messageData(msg);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (CoolRequestStarConfig.isDebug()) {
            System.out.println(ComponentLoader.class.getName() + " run:");
        }
        String json = applicationContext.getEnvironment().getProperty("cool.request.plugin.json");

        availableTcpPort = SocketUtils.findAvailableTcpPort();
        pluginCommunication.startServer(availableTcpPort);
        SpringBootStartInfo springBootStartInfo = new SpringBootStartInfo();
        springBootStartInfo.setAvailableTcpPort(availableTcpPort);
        for (ComponentSupport componentLoader : componentLoaders) {
            if (componentLoader.canSupport(this.applicationContext)) {
                componentDataHandlers.add(componentLoader.start(this.applicationContext, springBootStartInfo,JsonMapperFactory.getJsonMapper(json)));
            }
        }
        for (ComponentDataHandler componentDataHandler : componentDataHandlers) {
            if (componentDataHandler != null) {
                try {
                    if (CoolRequestStarConfig.isDebug()){
                        System.out.println(componentDataHandler);
                    }
                    componentDataHandler.publishData(applicationContext);
                } catch (Exception e) {
                    if (CoolRequestStarConfig.isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
