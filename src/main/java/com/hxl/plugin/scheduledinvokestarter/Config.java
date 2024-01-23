package com.hxl.plugin.scheduledinvokestarter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static final Path CONFIG_WORK_HOME = Paths.get(System.getProperty("user.home"), ".config", "spring-invoke", "invoke", "lib");
    public static final String SPRING_TEST_5="spring-test-5.3.30.jar";
    public static final String SPRING_TEST_6="spring-test-6.0.13.jar";
    public static final String SPRING_TEST_4="spring-test-4.0.0.jar";
    public static String getLibPath() {
        if (!CONFIG_WORK_HOME.toFile().exists()) {
            try {
                Files.createDirectories(CONFIG_WORK_HOME);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return CONFIG_WORK_HOME.toString();
    }
}
