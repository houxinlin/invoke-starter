package com.hxl.plugin.scheduledinvokestarter.components.spring.gateway;

import com.hxl.plugin.scheduledinvokestarter.MockClassLoader;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

public class EnabledSpringGateway implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        return false;
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        return null;
    }
    //    @Bean
//    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//    @ConditionalOnClass(name = "org.springframework.cloud.gateway.route.RouteLocator")
//    public Object springGatewayComponent() {
//        try {
//            return Class.forName("com.hxl.plugin.scheduledinvokestarter.components.spring.gateway.SpringGatewayComponent",
//                    true, new MockClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader())).getConstructor().newInstance();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
