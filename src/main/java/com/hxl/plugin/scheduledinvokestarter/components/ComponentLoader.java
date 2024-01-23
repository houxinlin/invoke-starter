package com.hxl.plugin.scheduledinvokestarter.components;

import com.hxl.plugin.scheduledinvokestarter.PluginCommunication;
import com.hxl.plugin.scheduledinvokestarter.components.spring.controller.EnabledSpringMvcRequestMapping;
import com.hxl.plugin.scheduledinvokestarter.components.spring.gateway.EnabledSpringGateway;
import com.hxl.plugin.scheduledinvokestarter.utils.SocketUtils;
import com.hxl.plugin.scheduledinvokestarter.utils.SystemUtils;
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
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void pluginMessage(String msg) {
        if (SystemUtils.isDebug()) {
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
        availableTcpPort = SocketUtils.findAvailableTcpPort();
        pluginCommunication.startServer(availableTcpPort);
        SpringBootStartInfo springBootStartInfo = new SpringBootStartInfo();
        springBootStartInfo.setAvailableTcpPort(availableTcpPort);
        for (ComponentSupport componentLoader : componentLoaders) {
            if (componentLoader.canSupport(this.applicationContext)) {
                componentDataHandlers.add(componentLoader.start(this.applicationContext, springBootStartInfo));
            }
        }
        for (ComponentDataHandler componentDataHandler : componentDataHandlers) {
            if (componentDataHandler != null) {
                componentDataHandler.publishData(applicationContext);
            }
        }
    }
}
