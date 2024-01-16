package com.hxl.plugin.scheduledinvokestarter.utils;

public class SystemUtils {
    public static boolean isDebug() {
        return System.getProperty("hxl.cool.request.debug", "false").equals("true");
    }

    public static String getProjectName(String def) {
        return System.getProperty("hxl.spring.request.project", def);
    }
    public static String getModuleName(String def) {
        return System.getProperty("hxl.spring.request.module", def);
    }

}
