package com.reevesclient.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter HM_FORMAT   = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HMS_FORMAT  = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter HM12_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private TimeUtil() {}

    public static String currentTime24h()  { return LocalDateTime.now().format(HM_FORMAT); }
    public static String currentTime24hS() { return LocalDateTime.now().format(HMS_FORMAT); }
    public static String currentTime12h()  { return LocalDateTime.now().format(HM12_FORMAT); }

    public static long nowMs()   { return System.currentTimeMillis(); }
    public static long nowNano() { return System.nanoTime(); }

    /** Returns elapsed milliseconds since a stored timestamp. */
    public static long elapsedMs(long startMs) { return nowMs() - startMs; }
}
