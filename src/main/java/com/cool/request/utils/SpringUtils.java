package com.cool.request.utils;

import org.springframework.context.ApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringUtils {
    private static final Pattern TRIM_PATTERN = Pattern.compile("^/*(.*?)/*$");
    private static final char SLASH = '/';

    public static int getServerPort(ApplicationContext applicationContext) {
        String port = applicationContext.getEnvironment().getProperty("server.port");
        if (port == null || "0".equalsIgnoreCase(port)) return 8080;
        return Integer.parseInt(port);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isBlank(String str) {
        return (str == null || str.trim().isEmpty());
    }
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    private static String trimPath(String value) {
        final Matcher matcher = TRIM_PATTERN.matcher(value);
        return matcher.find() && isNotBlank(matcher.group(1)) ? matcher.group(1) : null;
    }

    public static String collectPath(String... pathParts) {
        final StringBuilder sb = new StringBuilder();
        for (String item : pathParts) {
            if (isBlank(item)) {
                continue;
            }
            final String path = trimPath(item);
            if (isNotBlank(path)) {
                sb.append(SLASH).append(path);
            }
        }
        return sb.length() > 0 ? sb.toString() : String.valueOf(SLASH);
    }

    public static String getContextPath(ApplicationContext applicationContext) {
        String contextPath = applicationContext.getEnvironment().getProperty("server.servlet.context-path");
        String servletPath = applicationContext.getEnvironment().getProperty("spring.mvc.servlet.path");
        if (isEmpty(contextPath) && isEmpty(servletPath)) return "";

        if (isEmpty(contextPath)) return servletPath;
        if (isEmpty(servletPath)) return contextPath;
        return collectPath(contextPath, servletPath);
    }
}
