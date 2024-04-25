package com.cool.request;

import com.cool.request.utils.ApplicationHome;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CoolRequestProjectLog {
    private static final Logger logger = Logger.getLogger("Cool Request");
    private static FileHandler fileHandler = null;

    static {
        init();
    }

    private static String getLogName() {
        if (!Config.LOG_HOME.toFile().exists()) {
            Config.LOG_HOME.toFile().mkdirs();
        }
        String absolutePath = new ApplicationHome().getDir().getAbsolutePath();
        return Paths.get(Config.LOG_HOME.toString(), absolutePath.replace(":", "-")
                .replace(File.separator, "-") + ".log").toString();
    }

    public static void log(String log) {
        if (logger == null || fileHandler == null) return;
        logger.info(log);
        fileHandler.flush();
    }

    @SuppressWarnings("All")
    public static void userExceptionLog(Throwable log) {
        log.printStackTrace();
    }

    @SuppressWarnings("All")
    public static void logWithDebug(Throwable log) {
        if (logger == null) return;
        logger.info(log.getMessage());
    }


    public static void init() {
        try {
            fileHandler = new FileHandler(getLogName(), 1024 * 1000, 1, false);
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
