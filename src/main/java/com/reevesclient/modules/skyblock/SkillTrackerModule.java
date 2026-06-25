package com.reevesclient.modules.skyblock;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import com.reevesclient.core.util.TextUtil;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.regex.*;

/**
 * Tracks SkyBlock skill XP by parsing action bar and chat messages.
 * The data visible here is purely what the server already shows the player — no hidden data.
 */
public class SkillTrackerModule extends Module {

    public static final class SkillData {
        public String name;
        public long   currentXP;
        public long   levelXP;   // XP needed for this level
        public long   totalXP;
        public int    level;
        public double percentToNext;

        public SkillData(String name) { this.name = name; }
    }

    // XP table for skills (standard Hypixel SkyBlock values, levels 1-60)
    private static final long[] XP_TABLE = {
        0, 50, 125, 200, 300, 500, 750, 1000, 1500, 2000,
        3500, 5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000, 100000,
        150000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000,
        1100000, 1200000, 1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000,
        2100000, 2200000, 2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000,
        3700000, 4000000, 4300000, 4600000, 4900000, 5200000, 5500000, 5800000, 6100000, 6400000
    };

    // Pattern: "+123.4 Farming (12,345.6/50,000 XP)"
    private static final Pattern XP_PATTERN = Pattern.compile(
        "\\+([\\d,]+(?:\\.\\d+)?)\\s+(\\w+)\\s+\\(([\\d,]+(?:\\.\\d+)?)/([\\d,]+(?:\\.\\d+)?)\\s+XP\\)");

    private final Map<String, SkillData> skills = new LinkedHashMap<>();
    private String activeSkill = "";

    private final BooleanSetting showOverlay;
    private final BooleanSetting showInChat;

    public SkillTrackerModule() {
        super("skill_tracker", "Skill Tracker",
              "Tracks SkyBlock skill XP and level progress from action-bar data.",
              ModuleCategory.SKYBLOCK, true);

        showOverlay = addSetting(new BooleanSetting(
            "show_overlay", "Show HUD Overlay",
            "Display active skill progress above the action bar.", true));

        showInChat = addSetting(new BooleanSetting(
            "show_in_chat", "Log to Chat",
            "Post a skill level-up notification to local chat.", true));

        initSkills();
    }

    private void initSkills() {
        for (String s : List.of("Farming", "Mining", "Combat", "Foraging", "Fishing",
                                 "Enchanting", "Alchemy", "Carpentry", "Runecrafting", "Social")) {
            skills.put(s, new SkillData(s));
        }
    }

    /**
     * Called from MixinInGameHud when the action bar text is about to be rendered.
     * Returns modified text (unchanged — we only read it).
     */
    public void parseActionBar(String text) {
        if (!isEnabled()) return;
        Matcher m = XP_PATTERN.matcher(text);
        if (!m.find()) return;

        String skillName = m.group(2);
        SkillData data = skills.get(skillName);
        if (data == null) {
            data = new SkillData(skillName);
            skills.put(skillName, data);
        }

        double currentXP = parseDouble(m.group(3));
        double levelXP   = parseDouble(m.group(4));

        int prevLevel    = data.level;
        data.level       = xpToLevel((long) currentXP);
        data.currentXP   = (long) currentXP;
        data.levelXP     = (long) levelXP;
        data.percentToNext = levelXP > 0 ? (currentXP / levelXP) * 100.0 : 100.0;
        activeSkill      = skillName;

        if (data.level > prevLevel && showInChat.getValue() && prevLevel > 0) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                    "§a[Reeves] §f" + skillName + " leveled up to §e" + data.level + "§f!"), false);
            }
        }
    }

    private int xpToLevel(long xp) {
        int level = 0;
        long cumulative = 0;
        for (int i = 0; i < XP_TABLE.length && cumulative + XP_TABLE[i] <= xp; i++) {
            cumulative += XP_TABLE[i];
            level = i + 1;
        }
        return level;
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    public Map<String, SkillData> getSkills()   { return Collections.unmodifiableMap(skills); }
    public String                 getActiveSkill() { return activeSkill; }
    public boolean                isShowOverlay()  { return showOverlay.getValue(); }
}
