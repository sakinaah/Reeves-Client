package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

/**
 * Converts the real-world timestamp into SkyBlock calendar date, season, and time.
 * Formula is public knowledge derived from the known SkyBlock epoch.
 */
public class SkyBlockCalendarModule extends Module {

    // SkyBlock started at Unix epoch 1560275700000 ms (June 11, 2019 17:15:00 UTC)
    private static final long SB_EPOCH_MS  = 1560275700000L;
    private static final long SB_YEAR_MS   = 446400000L;  // 124 real hours per SkyBlock year
    private static final long SB_MONTH_MS  = 37200000L;   // 124/12 * 3600000
    private static final long SB_DAY_MS    = 1200000L;    // 20 real minutes per SkyBlock day

    private static final String[] MONTHS = {
        "Early Spring", "Spring", "Late Spring",
        "Early Summer", "Summer", "Late Summer",
        "Early Autumn", "Autumn", "Late Autumn",
        "Early Winter", "Winter", "Late Winter"
    };

    private static final String[] MAYORS = {
        "Aatrox", "Cole", "Diana", "Diaz", "Finnegan",
        "Foxy", "Marina", "Paul", "Scorpius", "Technoblade", "Jerry"
    };

    public record CalendarState(int year, String month, int day, String timeOfDay, String season) {}

    public CalendarState getCurrentState() {
        long now  = System.currentTimeMillis();
        long elapsed = now - SB_EPOCH_MS;

        int  year  = (int) (elapsed / SB_YEAR_MS) + 1;
        long inYear = elapsed % SB_YEAR_MS;
        int  monthIdx = (int) (inYear / SB_MONTH_MS);
        long inMonth = inYear % SB_MONTH_MS;
        int  day   = (int) (inMonth / SB_DAY_MS) + 1;
        long inDay = inMonth % SB_DAY_MS;

        // 1200000ms per day → 24000 ticks → map to hours
        long ticks = (inDay * 24000) / SB_DAY_MS;
        int  hour  = (int) ((ticks / 1000 + 6) % 24);
        int  minute = (int) ((ticks % 1000) * 60 / 1000);

        String timeOfDay = String.format("%02d:%02d", hour, minute);
        String monthName = monthIdx < MONTHS.length ? MONTHS[monthIdx] : "Unknown";

        String season;
        if (monthIdx < 3) season = "Spring";
        else if (monthIdx < 6) season = "Summer";
        else if (monthIdx < 9) season = "Autumn";
        else season = "Winter";

        return new CalendarState(year, monthName, day, timeOfDay, season);
    }

    public String getCurrentMayor() {
        long now = System.currentTimeMillis();
        long elapsed = now - SB_EPOCH_MS;
        int year = (int) (elapsed / SB_YEAR_MS);
        return MAYORS[year % MAYORS.length];
    }

    public SkyBlockCalendarModule() {
        super("skyblock_calendar", "SkyBlock Calendar",
              "Shows current SkyBlock date, season, time, and mayor.",
              ModuleCategory.SKYBLOCK, true);
    }
}
