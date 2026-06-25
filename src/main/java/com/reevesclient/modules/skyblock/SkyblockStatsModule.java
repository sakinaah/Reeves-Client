package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the SkyBlock action bar for the player's Health, Mana and Defense and
 * exposes them for the RPG bars HUD. The action bar is read via the InGameHud
 * mixin (same path the skill/garden parsers use). Informational only.
 */
public class SkyblockStatsModule extends Module {

    // Action-bar symbols: ❤ health, ✎ mana (intelligence), ❈ defense.
    private static final Pattern HEALTH  = Pattern.compile("(\\d+)/(\\d+)❤");
    private static final Pattern MANA    = Pattern.compile("(\\d+)/(\\d+)✎");
    private static final Pattern DEFENSE = Pattern.compile("(\\d+)❈");

    private int curHealth = 0, maxHealth = 0;
    private int curMana = 0,  maxMana = 0;
    private int defense = 0;
    private long lastUpdateMs = 0;

    private final BooleanSetting hideVanillaBars;

    public SkyblockStatsModule() {
        super("skyblock_stats", "RPG Stat Bars",
                "Shows SkyBlock Health / Mana / Defense bars from the action bar.",
                ModuleCategory.SKYBLOCK, false);
        hideVanillaBars = addSetting(new BooleanSetting("hide_vanilla", "Hide Vanilla Bars",
                "Hide vanilla health/hunger while in SkyBlock.", true));
    }

    /** Called from the InGameHud mixin with the stripped action-bar text. */
    public void parseActionBar(String plain) {
        if (!isEnabled() || plain == null) return;
        boolean any = false;
        Matcher h = HEALTH.matcher(plain);
        if (h.find()) { curHealth = parse(h.group(1)); maxHealth = parse(h.group(2)); any = true; }
        Matcher m = MANA.matcher(plain);
        if (m.find()) { curMana = parse(m.group(1)); maxMana = parse(m.group(2)); any = true; }
        Matcher d = DEFENSE.matcher(plain);
        if (d.find()) { defense = parse(d.group(1)); any = true; }
        if (any) lastUpdateMs = System.currentTimeMillis();
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    /** True if we've seen stat data recently (so the HUD knows there's something to show). */
    public boolean hasRecentData() { return maxHealth > 0 && System.currentTimeMillis() - lastUpdateMs < 5000; }

    public boolean hideVanillaBars() { return hideVanillaBars.getValue(); }

    public int getCurHealth() { return curHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurMana()   { return curMana; }
    public int getMaxMana()   { return maxMana; }
    public int getDefense()   { return defense; }
}
