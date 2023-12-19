package com.hxl.plugin.scheduledinvokestarter;

public class SystemUtils {
    public static boolean isDebug() {
        return System.getProperty("hxl.cool.request.debug", "false").equals("true");
    }
}
