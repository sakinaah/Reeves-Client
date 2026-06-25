package com.reevesclient.modules.dungeons;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;

import java.util.regex.*;

/**
 * Tracks the current dungeon run score by parsing scoreboard data.
 * Calculates skill score, speed score, and estimated grade.
 * Informational only.
 */
public class DungeonScoreModule extends Module {

    private int    skillScore  = 100; // starts at 100, deducted for deaths
    private int    speedScore  = 0;
    private int    secretScore = 0;
    private int    bonusScore  = 0;
    private int    deaths      = 0;
    private int    totalSecrets = 0;
    private int    foundSecrets = 0;
    private long   runStartMs  = 0;

    private static final Pattern SCORE_PATTERN  = Pattern.compile("Score:\\s*(\\d+)");
    private static final Pattern SECRET_PATTERN = Pattern.compile("Secrets\\s*Found:\\s*(\\d+)/(\\d+)");
    private static final Pattern DEATH_PATTERN  = Pattern.compile("Deaths:\\s*(\\d+)");

    public DungeonScoreModule() {
        super("dungeon_score", "Dungeon Score Tracker",
              "Tracks the current run score and estimates final grade.",
              ModuleCategory.DUNGEONS, true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInDungeon() || client.world == null) return;
        parseScoreboard(client);
        if (runStartMs == 0) runStartMs = System.currentTimeMillis();
        computeSpeedScore();
    }

    private void parseScoreboard(MinecraftClient client) {
        var scoreboard = client.world.getScoreboard();
        var objective  = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return;

        for (var holder : scoreboard.getKnownScoreHolders()) {
            String line = com.reevesclient.core.util.TextUtil.stripFormatting(holder.getNameForScoreboard());
            Matcher sm = SECRET_PATTERN.matcher(line);
            if (sm.find()) {
                foundSecrets = Integer.parseInt(sm.group(1));
                totalSecrets = Integer.parseInt(sm.group(2));
            }
            Matcher dm = DEATH_PATTERN.matcher(line);
            if (dm.find()) {
                deaths = Integer.parseInt(dm.group(1));
            }
        }

        skillScore  = Math.max(0, 100 - deaths * 2);
        secretScore = totalSecrets > 0 ? (int) Math.round((foundSecrets * 40.0) / totalSecrets) : 0;
    }

    private void computeSpeedScore() {
        if (runStartMs == 0) return;
        long elapsedMinutes = (System.currentTimeMillis() - runStartMs) / 60000;
        // Scoring: S+ < 20min, S < 25, A < 30, B < 35, C < 40
        if (elapsedMinutes < 20)      speedScore = 100;
        else if (elapsedMinutes < 25) speedScore = 75;
        else if (elapsedMinutes < 30) speedScore = 50;
        else if (elapsedMinutes < 35) speedScore = 25;
        else                          speedScore = 10;
    }

    public int    getTotalScore()   { return skillScore + speedScore + secretScore + bonusScore; }
    public String getEstimatedGrade() {
        int total = getTotalScore();
        if (total >= 300) return "S+";
        if (total >= 270) return "S";
        if (total >= 240) return "A";
        if (total >= 175) return "B";
        if (total >= 100) return "C";
        return "D";
    }

    public int  getSkillScore()   { return skillScore; }
    public int  getSpeedScore()   { return speedScore; }
    public int  getSecretScore()  { return secretScore; }
    public int  getDeaths()       { return deaths; }
    public int  getFoundSecrets() { return foundSecrets; }
    public int  getTotalSecrets() { return totalSecrets; }

    public void onNewRun() {
        skillScore = 100; speedScore = 0; secretScore = 0;
        bonusScore = 0; deaths = 0; totalSecrets = 0;
        foundSecrets = 0; runStartMs = 0;
    }
}
