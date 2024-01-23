package com.hxl.plugin.scheduledinvokestarter.components.spring.gateway;

import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;

public class EnabledSpringGateway implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        try {
            Class.forName("org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext");
            return applicationContext instanceof ReactiveWebApplicationContext;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        return (new SpringGatewayComponent(applicationContext, springBootStartInfo));

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
