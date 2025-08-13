// Logger.java
package org.lpc.utils;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Logger {
    private static final String APP_NAME = "Triton-64 VM";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static void log(String format, Object... args) {
        String timestamp = TIME_FORMAT.format(new Date());
        System.out.printf("[%s] [%s] %s%n",
                timestamp,
                APP_NAME,
                String.format(format, args));
    }

    public static void logError(Throwable t) {
        String timestamp = TIME_FORMAT.format(new Date());
        System.err.printf("[%s] [%s] ERROR: %s%n",
                timestamp, APP_NAME, t.getMessage());
        t.printStackTrace(System.err);
    }
}