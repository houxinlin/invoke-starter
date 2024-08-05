package com.cool.request.utils;

import org.springframework.context.ApplicationContext;

import java.nio.file.Paths;

public class SpringUtils {
    public static int getServerPort(ApplicationContext applicationContext) {
        String port = applicationContext.getEnvironment().getProperty("server.port");
        if (port == null || "0".equalsIgnoreCase(port)) return 8080;
        return Integer.parseInt(port);
    }

    public static String getContextPath(ApplicationContext applicationContext) {
        String contextPath = applicationContext.getEnvironment().getProperty("server.servlet.context-path");
        String servletPath = applicationContext.getEnvironment().getProperty("spring.mvc.servlet.path");
        if (contextPath == null && servletPath == null) return "";

        if (servletPath == null) return contextPath;
        if (contextPath == null) return servletPath;
        return Paths.get("/", contextPath, servletPath).toString();
    }
}
