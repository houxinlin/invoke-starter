package com.cool.request.utils;


import org.springframework.core.SpringVersion;

import java.lang.reflect.Method;

public class VersionUtils {
    public static boolean isSpring5() {
        try {
            String version = SpringVersion.getVersion();
            return Integer.valueOf(version.split("\\.")[0]) >= 5;
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean is5Dot3() {
        try {
            Class.forName("org.springframework.web.util.ServletRequestPathUtils");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static boolean isSpringBoot3Dot0() {
        try {
            Class<?> springBootVersionClass = Class.forName("org.springframework.boot.SpringBootVersion");
            Method getVersionMethod = springBootVersionClass.getDeclaredMethod("getVersion");
            String result = (String) getVersionMethod.invoke(null);
            String[] split = result.split("\\.");
            return Integer.parseInt(split[0]) >= 3;
        } catch (Exception e) {
            return false;
        }

    }
}
