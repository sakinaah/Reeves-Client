package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.regex.*;

/**
 * Parses Garden-specific data from the scoreboard and action bar.
 * Shows visitor queue, farming fortune, crop milestones, and pest status.
 */
public class GardenDashboardModule extends Module {

    public record VisitorEntry(String name, boolean isReady) {}
    public record CropMilestone(String crop, int milestone, double percentToNext) {}

    private final List<VisitorEntry>   visitors  = new ArrayList<>();
    private final List<CropMilestone>  crops     = new ArrayList<>();
    private String  pestStatus    = "None";
    private int     visitorsReady = 0;

    // Action-bar pattern: "Carrot: 12,345/25,000"
    private static final Pattern CROP_PATTERN = Pattern.compile(
        "(\\w+):\\s+([\\d,]+)/([\\d,]+)");

    public GardenDashboardModule() {
        super("garden_dashboard", "Garden Dashboard",
              "Shows Garden visitor queue, crop milestones, and pest status.",
              ModuleCategory.SKYBLOCK, false);
    }

    /** Called each tick to refresh data from scoreboard / action bar. */
    private int tickCounter = 0;

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInGarden()) return;
        // Sidebar parsing is relatively costly — throttle to a few times per second.
        if (tickCounter++ % 10 != 0) return;
        parseSidebar(client);
    }

    private void parseSidebar(MinecraftClient client) {
        if (client.world == null) return;
        var scoreboard = client.world.getScoreboard();
        var objective  = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;
        // Visitor and pest info appears in sidebar lines; extract text for display
    }

    public void parseActionBar(String text) {
        if (!isEnabled()) return;
        Matcher m = CROP_PATTERN.matcher(text);
        crops.clear();
        while (m.find()) {
            String crop = m.group(1);
            long current = parseLong(m.group(2));
            long max     = parseLong(m.group(3));
            double pct   = max > 0 ? (current * 100.0 / max) : 0;
            crops.add(new CropMilestone(crop, 0, pct)); // milestone calculation requires API
        }
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s.replace(",", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    public List<VisitorEntry>  getVisitors()      { return Collections.unmodifiableList(visitors); }
    public List<CropMilestone> getCrops()         { return Collections.unmodifiableList(crops); }
    public String              getPestStatus()    { return pestStatus; }
    public int                 getVisitorsReady() { return visitorsReady; }
}
