package com.hxl.plugin.scheduledinvokestarter.components.spring.controller;

import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class EnabledSpringMvcRequestMapping implements ComponentSupport {

    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        try {
            Class.forName("org.springframework.web.context.WebApplicationContext");
            Class<?> aClass = Class.forName("com.hxl.plugin.scheduledinvokestarter.components.spring.controller.SpringWebApplicationCheck");
            MethodType methodType = MethodType.methodType(boolean.class, ApplicationContext.class);
            MethodHandle test = MethodHandles.lookup().findStatic(aClass, "check", methodType);
            return (boolean) test.invoke(applicationContext);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return (new SpringRequestMappingComponent(applicationContext, springBootStartInfo));

    }
}
