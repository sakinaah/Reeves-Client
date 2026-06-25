package com.reevesclient.core.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Detects Hypixel game mode context from scoreboard and tab-list data. */
public final class HypixelUtil {

    private static final String HYPIXEL_HOST = "hypixel.net";

    private HypixelUtil() {}

    // ── Scoreboard / tab-list readers ─────────────────────────────────────────

    /**
     * Reads the sidebar lines the way Hypixel actually renders them: each visible
     * line is the score holder's team prefix + name + suffix (Hypixel stores the
     * text in the team, not the holder name). Returns plain, formatting-stripped
     * strings, unordered.
     */
    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return lines;
        Scoreboard sb = mc.world.getScoreboard();
        ScoreboardObjective obj = sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (obj == null) return lines;
        for (ScoreHolder holder : sb.getKnownScoreHolders()) {
            if (!sb.getScoreHolderObjectives(holder).containsKey(obj)) continue;
            String name = holder.getNameForScoreboard();
            Team team = sb.getScoreHolderTeam(name);
            String line = team != null
                    ? Team.decorateName(team, Text.literal(name)).getString()
                    : name;
            lines.add(TextUtil.stripFormatting(line));
        }
        return lines;
    }

    /** Returns the sidebar objective title (formatting stripped), or "". */
    public static String getSidebarTitle() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return "";
        ScoreboardObjective obj = mc.world.getScoreboard()
                .getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        return obj == null ? "" : TextUtil.stripFormatting(obj.getDisplayName().getString());
    }

    /** Returns the tab-list entry display names (formatting stripped, blanks dropped). */
    public static List<String> getTabListLines() {
        List<String> out = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        var handler = mc.getNetworkHandler();
        if (handler == null) return out;
        for (PlayerListEntry e : handler.getPlayerList()) {
            Text dn = e.getDisplayName();
            if (dn == null) continue;
            String s = TextUtil.stripFormatting(dn.getString()).trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    /**
     * Extracts the SkyBlock item id from an item's {@code ExtraAttributes} custom
     * data (e.g. "HYPERION", "ENCHANTED_DIAMOND"), or "" if not a SkyBlock item.
     */
    public static String getSkyblockId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return "";
        NbtCompound nbt = custom.copyNbt();
        NbtCompound extra = nbt.getCompoundOrEmpty("ExtraAttributes");
        return extra.getString("id", "");
    }

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
