package com.cool.request.components.method;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.ComponentSupport;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.json.JsonMapper;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodComponentSupport implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        try {
            Class.forName("org.springframework.web.context.WebApplicationContext");
            Class<?> aClass = Class.forName("com.cool.request.components.spring.controller.SpringWebApplicationCheck");
            MethodType methodType = MethodType.methodType(boolean.class, ApplicationContext.class);
            MethodHandle test = MethodHandles.lookup().findStatic(aClass, "check", methodType);
            CoolRequestProjectLog.log("Spring MVC  组件可被加载");
            return (boolean) test.invoke(applicationContext);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return new MethodComponentDataHandler(applicationContext, springBootStartInfo);
    }
}
