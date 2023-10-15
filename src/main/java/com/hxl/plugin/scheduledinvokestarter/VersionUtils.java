package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.boot.SpringBootVersion;

public class VersionUtils {
    public static boolean is5Dot3() {
        try {
            Class.forName("org.springframework.web.util.ServletRequestPathUtils");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    public static boolean isSpringBoot3Dot0() {
        String[] split = SpringBootVersion.getVersion().split("\\.");
        return Integer.parseInt(split[0]) >= 3;
    }
}
