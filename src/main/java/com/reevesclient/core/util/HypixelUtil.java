package com.reevesclient.core.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

/** Detects Hypixel game mode context from scoreboard and tab-list data. */
public final class HypixelUtil {

    private static final String HYPIXEL_HOST = "hypixel.net";

    private HypixelUtil() {}

    /** Returns true if the client is currently connected to Hypixel. */
    public static boolean isOnHypixel() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo info = mc.getCurrentServerEntry();
        if (info == null) return false;
        return info.address.toLowerCase().contains(HYPIXEL_HOST);
    }

    /** Best-effort SkyBlock detection via scoreboard title. */
    public static boolean isInSkyBlock() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        var scoreboard = mc.world.getScoreboard();
        var objective  = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;
        String title = TextUtil.stripFormatting(objective.getDisplayName().getString());
        return title.contains("SKYBLOCK");
    }

    /** Detects Dungeon presence from scoreboard. */
    public static boolean isInDungeon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        var scoreboard = mc.world.getScoreboard();
        var objective  = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;
        String title = TextUtil.stripFormatting(objective.getDisplayName().getString());
        return title.contains("CATACOMBS") || title.contains("DUNGEON");
    }

    /** Detects Garden zone. */
    public static boolean isInGarden() {
        if (!isInSkyBlock()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        // Garden has a specific area name in sidebar lines – approximate detection
        return checkSidebarContains("Garden");
    }

    public static boolean checkSidebarContains(String substring) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return false;
        var scoreboard = mc.world.getScoreboard();
        var objective  = scoreboard.getObjectiveForSlot(
                net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return false;
        for (var entry : scoreboard.getKnownScoreHolders()) {
            var scores = scoreboard.getScoreHolderObjectives(entry);
            if (scores.containsKey(objective)) {
                if (entry.getNameForScoreboard().contains(substring)) return true;
            }
        }
        return false;
    }
}
