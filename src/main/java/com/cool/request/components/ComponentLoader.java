package com.cool.request.components;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.components.method.MethodComponentSupport;
import com.cool.request.components.spring.controller.EnabledSpringMvcRequestMapping;
import com.cool.request.components.spring.gateway.EnabledSpringGateway;
import com.cool.request.components.xxljob.XxlJobComponentSupport;
import com.cool.request.json.JsonMapper;
import com.cool.request.json.JsonMapperFactory;
import com.cool.request.rmi.plugin.ICoolRequestPluginRMI;
import com.cool.request.rmi.starter.CoolRequestStarterRMIImpl;
import com.cool.request.rmi.starter.ICoolRequestStarterRMI;
import com.cool.request.utils.SocketUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import static com.cool.request.utils.SpringUtils.getServerPort;

/**
 * 组件数据加载器
 */
public class ComponentLoader implements
        CommandLineRunner,
        ApplicationContextAware {
    private final List<ComponentSupport> componentLoaders = new ArrayList<>();
    private final List<ComponentDataHandler> componentDataHandlers = new ArrayList<>();
    private ApplicationContext applicationContext;
    private final List<ComponentListener> componentListeners = new ArrayList<>();

    public ComponentLoader() {
        componentLoaders.add(new EnabledSpringMvcRequestMapping());
        componentLoaders.add(new EnabledSpringGateway());
        componentLoaders.add(new XxlJobComponentSupport());
        componentLoaders.add(new MethodComponentSupport());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<ComponentListener> getComponentListeners() {
        return componentListeners;
    }

    @Override
    public void run(String... args) {
        new Thread(new LoaderThread()).start();
    }

    private ICoolRequestStarterRMI createStarterRMI(int port) {
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            ICoolRequestStarterRMI coolRequestStarterRMI = new CoolRequestStarterRMIImpl(this);
            registry.bind(ICoolRequestStarterRMI.class.getName(), coolRequestStarterRMI);
        } catch (RemoteException | AlreadyBoundException ignored) {
        }
        return null;
    }

    class LoaderThread implements Runnable {
        @Override
        public void run() {
            //连接插件rmi
            String port = System.getProperty("hxl.spring.invoke.port");
            Registry registry;
            ICoolRequestPluginRMI coolRequestPluginRMI;
            try {
                registry = LocateRegistry.getRegistry("localhost", Integer.valueOf(port));
                Remote lookup = registry.lookup(ICoolRequestPluginRMI.class.getName());
                coolRequestPluginRMI = (ICoolRequestPluginRMI) lookup;

                String json = applicationContext.getEnvironment().getProperty("cool.request.plugin.json");
                int availableTcpPort = SocketUtils.findAvailableTcpPort();

                JsonMapper jsonMapper = JsonMapperFactory.getJsonMapper(json);
                if (jsonMapper == null) {
                    CoolRequestProjectLog.log("无法找到JSON解析库");
                }
                SpringBootStartInfo springBootStartInfo = new SpringBootStartInfo();
                springBootStartInfo.setAvailableTcpPort(availableTcpPort);
                springBootStartInfo.setJsonMapper(jsonMapper);
                springBootStartInfo.setCoolRequestPluginRMI(coolRequestPluginRMI);
                springBootStartInfo.setCoolRequestStarterRMI(createStarterRMI(availableTcpPort));

                //发送项目启动
                try {
                    if (springBootStartInfo.getCoolRequestPluginRMI() != null) {
                        springBootStartInfo.getCoolRequestPluginRMI().projectStartup(springBootStartInfo.getAvailableTcpPort(),
                                getServerPort(applicationContext));
                    }
                } catch (RemoteException ignored) {
                }

                for (ComponentSupport componentLoader : componentLoaders) {
                    if (componentLoader.canSupport(applicationContext)) {
                        ComponentDataHandler componentDataHandler =
                                componentLoader.start(applicationContext, springBootStartInfo, jsonMapper);
                        if (componentDataHandler instanceof ComponentListener) {
                            componentListeners.add(((ComponentListener) componentDataHandler));
                        }
                        componentDataHandlers.add(componentDataHandler);
                    }
                }
                CoolRequestProjectLog.log("组件库收集成功，共" + componentDataHandlers.size() + "个");
                for (ComponentDataHandler componentDataHandler : componentDataHandlers) {
                    if (componentDataHandler != null) {
                        try {
                            componentDataHandler.componentInit(applicationContext);
                        } catch (Exception e) {
                            CoolRequestProjectLog.logWithDebug(e);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
