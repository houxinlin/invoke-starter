package com.hxl.plugin.scheduledinvokestarter.utils;

import org.springframework.context.ApplicationContext;

public class SpringUtils {
    public static int getServerPort(ApplicationContext applicationContext) {
        String port = applicationContext.getEnvironment().getProperty("server.port");
        if (port == null || "0".equalsIgnoreCase(port)) return 8080;
        return Integer.parseInt(port);
    }

    public static String getContextPath(ApplicationContext applicationContext) {
        String contextPath = applicationContext.getEnvironment().getProperty("server.servlet.context-path");
        if (contextPath == null) return "";
        return contextPath;
    }
}
