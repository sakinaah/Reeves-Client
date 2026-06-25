package com.reevesclient.modules.dungeons;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the current Catacombs run by reading the sidebar (cleared %, floor) and
 * the tab list (secrets, deaths, crypts, puzzles). Produces an <em>estimated</em>
 * run score and grade.
 *
 * Informational only — it reads game state and renders text. It never interacts
 * with the world, so it is fully Hypixel-rule compliant.
 *
 * NOTE: Hypixel's exact score formula and line wording change over time; the
 * score below is a best-effort estimate and the parsing is intentionally
 * defensive. The displayed values (secrets/deaths/crypts/cleared) come straight
 * from the server text and are exact.
 */
public class DungeonScoreModule extends Module {

    // Parsed run state
    private String floor            = "";
    private int    clearedPct       = 0;
    private int    foundSecrets     = 0;
    private int    deaths           = 0;
    private int    crypts           = 0;
    private int    completedPuzzles = 0;
    private int    totalPuzzles     = 0;
    private long   runStartMs       = 0;

    // Display settings
    private final BooleanSetting showScore;
    private final BooleanSetting showSecrets;
    private final BooleanSetting showDeaths;
    private final BooleanSetting showCrypts;

    private static final Pattern FLOOR_PATTERN   = Pattern.compile("\\(([FM][1-7])\\)");
    private static final Pattern CLEARED_PATTERN = Pattern.compile("Cleared:\\s*(\\d+)%");
    private static final Pattern SECRETS_PATTERN = Pattern.compile("Secrets\\s*Found:\\s*(\\d+)");
    private static final Pattern DEATHS_PATTERN  = Pattern.compile("Deaths:\\s*(\\d+)");
    private static final Pattern CRYPTS_PATTERN  = Pattern.compile("Crypts:\\s*(\\d+)");
    private static final Pattern PUZZLES_PATTERN = Pattern.compile("Puzzles:\\s*\\((\\d+)\\)");

    public DungeonScoreModule() {
        super("dungeon_score", "Dungeon Tracker",
                "Tracks run score, secrets, deaths and crypts. Informational only.",
                ModuleCategory.DUNGEONS, true);
        showScore   = addSetting(new BooleanSetting("show_score",   "Show Score",   "Display the estimated run score.", true));
        showSecrets = addSetting(new BooleanSetting("show_secrets", "Show Secrets", "Display secrets found.", true));
        showDeaths  = addSetting(new BooleanSetting("show_deaths",  "Show Deaths",  "Display the team death count.", true));
        showCrypts  = addSetting(new BooleanSetting("show_crypts",  "Show Crypts",  "Display crypts blown.", true));
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || client.world == null || !HypixelUtil.isInDungeon()) {
            return;
        }
        if (runStartMs == 0) runStartMs = System.currentTimeMillis();

        parseFloor();
        parseSidebar();
        parseTabList();
    }

    private void parseFloor() {
        Matcher m = FLOOR_PATTERN.matcher(HypixelUtil.getSidebarTitle());
        if (m.find()) { floor = m.group(1); return; }
        for (String line : HypixelUtil.getSidebarLines()) {
            Matcher lm = FLOOR_PATTERN.matcher(line);
            if (lm.find()) { floor = lm.group(1); return; }
        }
    }

    private void parseSidebar() {
        for (String line : HypixelUtil.getSidebarLines()) {
            Matcher cm = CLEARED_PATTERN.matcher(line);
            if (cm.find()) clearedPct = parseIntSafe(cm.group(1), clearedPct);
        }
    }

    private void parseTabList() {
        int puzzlesDone = 0;
        for (String line : HypixelUtil.getTabListLines()) {
            Matcher sm = SECRETS_PATTERN.matcher(line);
            if (sm.find()) foundSecrets = parseIntSafe(sm.group(1), foundSecrets);

            Matcher dm = DEATHS_PATTERN.matcher(line);
            if (dm.find()) deaths = parseIntSafe(dm.group(1), deaths);

            Matcher crm = CRYPTS_PATTERN.matcher(line);
            if (crm.find()) crypts = parseIntSafe(crm.group(1), crypts);

            Matcher pm = PUZZLES_PATTERN.matcher(line);
            if (pm.find()) totalPuzzles = parseIntSafe(pm.group(1), totalPuzzles);

            // Solved puzzles are marked with a check mark in their tab line.
            if (line.contains("✔") || line.contains("✓")) puzzlesDone++;
        }
        completedPuzzles = Math.min(puzzlesDone, totalPuzzles);
    }

    private static int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    // ── Derived values ──────────────────────────────────────────────────────────

    public long getElapsedMs() {
        return runStartMs == 0 ? 0 : System.currentTimeMillis() - runStartMs;
    }

    public String getElapsedString() {
        long s = getElapsedMs() / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /**
     * Best-effort score estimate (Explore + Skill + Speed + Bonus). Approximate;
     * calibrate against live runs. The displayed component values are exact.
     */
    public int getEstimatedScore() {
        int explore = (int) Math.round(clearedPct * 0.6);          // weighted clear %

        int skill = 100 - Math.max(0, deaths) * 2                  // death penalty
                - Math.max(0, totalPuzzles - completedPuzzles) * 10; // unsolved puzzles
        skill = Math.max(0, Math.min(100, skill));

        long minutes = getElapsedMs() / 60000;
        int speed;
        if (minutes < 12)      speed = 100;
        else if (minutes < 18) speed = 80;
        else if (minutes < 24) speed = 60;
        else if (minutes < 30) speed = 40;
        else                   speed = 20;

        int bonus = Math.min(5, crypts);

        return explore + skill + speed + bonus;
    }

    public String getEstimatedGrade() {
        int total = getEstimatedScore();
        if (total >= 300) return "S+";
        if (total >= 270) return "S";
        if (total >= 230) return "A";
        if (total >= 160) return "B";
        if (total >= 100) return "C";
        return "D";
    }

    // ── Getters for the HUD ──────────────────────────────────────────────────────

    public String getFloor()            { return floor; }
    public int    getClearedPct()       { return clearedPct; }
    public int    getFoundSecrets()     { return foundSecrets; }
    public int    getDeaths()           { return deaths; }
    public int    getCrypts()           { return crypts; }
    public int    getCompletedPuzzles() { return completedPuzzles; }
    public int    getTotalPuzzles()     { return totalPuzzles; }

    public boolean showScore()   { return showScore.getValue(); }
    public boolean showSecrets() { return showSecrets.getValue(); }
    public boolean showDeaths()  { return showDeaths.getValue(); }
    public boolean showCrypts()  { return showCrypts.getValue(); }

    /** Resets per-run state. Called when entering a new dungeon. */
    public void onNewRun() {
        floor = "";
        clearedPct = 0;
        foundSecrets = 0;
        deaths = 0;
        crypts = 0;
        completedPuzzles = 0;
        totalPuzzles = 0;
        runStartMs = 0;
    }
}
