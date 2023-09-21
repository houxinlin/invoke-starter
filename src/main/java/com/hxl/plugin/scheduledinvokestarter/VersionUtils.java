package com.hxl.plugin.scheduledinvokestarter;

public class VersionUtils {
    public static boolean is5Dot3(){
        try {
            Class.forName("org.springframework.web.util.ServletRequestPathUtils");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }
}
