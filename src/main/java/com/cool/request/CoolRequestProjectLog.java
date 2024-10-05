package com.cool.request;

import java.util.logging.Logger;

public class CoolRequestProjectLog {
    private static final Logger logger = Logger.getLogger("Cool Request");

    public static void log(String log) {
    }

    @SuppressWarnings("All")
    public static void userExceptionLog(Throwable log) {
        log.printStackTrace();
    }

}
