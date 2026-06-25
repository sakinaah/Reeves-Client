package com.reevesclient.core.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/** String and number formatting utilities. */
public final class TextUtil {

    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat ONE_DECIMAL  = new DecimalFormat("#,##0.0");
    private static final DecimalFormat TWO_DECIMAL  = new DecimalFormat("#,##0.00");

    private TextUtil() {}

    public static String formatInt(long value)         { return COMMA_FORMAT.format(value); }
    public static String formatDecimal(double value)   { return ONE_DECIMAL.format(value); }
    public static String formatCoins(double value)     { return TWO_DECIMAL.format(value); }

    /** Abbreviates large numbers: 1500 → 1.5k, 2000000 → 2M, etc. */
    public static String formatShort(long value) {
        if (value < 0) return "-" + formatShort(-value);
        if (value >= 1_000_000_000L) return ONE_DECIMAL.format(value / 1_000_000_000.0) + "B";
        if (value >= 1_000_000L)     return ONE_DECIMAL.format(value / 1_000_000.0)     + "M";
        if (value >= 1_000L)         return ONE_DECIMAL.format(value / 1_000.0)          + "k";
        return String.valueOf(value);
    }

    /** e.g. 3723 → "1h 2m 3s" */
    public static String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return String.format("%dh %dm %ds", h, m, s);
        if (m > 0) return String.format("%dm %ds", m, s);
        return String.format("%ds", s);
    }

    /** e.g. 3723 → "01:02:03" */
    public static String formatClock(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return String.format("%02d:%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }

    /** Pads a string to a minimum length with spaces. */
    public static String padLeft(String s, int len) {
        return String.format("%" + len + "s", s);
    }

    public static String padRight(String s, int len) {
        return String.format("%-" + len + "s", s);
    }

    /** Capitalizes the first character. */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /** Strips Minecraft §-codes from a string. */
    public static String stripFormatting(String s) {
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    /** Converts a SkyBlock-style item ID (e.g. RABBIT_FOOT) to display name. */
    public static String skyblockIdToName(String id) {
        if (id == null) return "Unknown";
        return capitalize(id.replace('_', ' '));
    }

    /** Formats XP progress as a percentage bar string. */
    public static String xpBar(long current, long max, int barLength) {
        int filled = (int) Math.round(barLength * Math.min(1.0, (double) current / max));
        return "█".repeat(filled) + "░".repeat(barLength - filled);
    }
}
