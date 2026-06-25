package com.reevesclient.modules.dungeons;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the current dungeon boss and phase by reading boss chat lines
 * ({@code [BOSS] <Name>: ...}). For F7/M7 the Necron sequence
 * (Maxor → Storm → Goldor → Necron → Wither King) is mapped to numbered phases.
 *
 * Informational only. The exact boss wording REQUIRES RUNTIME VERIFICATION, so
 * parsing is defensive and simply tracks the most recent speaker.
 */
public class DungeonBossModule extends Module {

    private static final Pattern BOSS_LINE = Pattern.compile("\\[BOSS\\]\\s*([^:]+):");

    private String currentBoss = "";
    private int    phase = 0;          // F7/M7 phase index (1–4), 0 if unknown
    private boolean bossActive = false;

    public DungeonBossModule() {
        super("dungeon_boss", "Boss Phase Tracker",
                "Tracks the current dungeon boss and F7/M7 phase. Informational only.",
                ModuleCategory.DUNGEONS, true);
    }

    public void onChat(String plain) {
        if (!isEnabled() || plain == null) return;
        Matcher m = BOSS_LINE.matcher(plain);
        if (m.find()) {
            String name = m.group(1).trim();
            currentBoss = name;
            bossActive = true;
            phase = switch (name.toLowerCase()) {
                case "maxor"  -> 1;
                case "storm"  -> 2;
                case "goldor" -> 3;
                case "necron" -> 4;
                default       -> phase; // other floors' bosses keep phase 0
            };
        }
        if (plain.contains("Necron: All this, for nothing")
                || plain.contains("The Catacombs") && plain.contains("Defeated")) {
            bossActive = false;
        }
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInDungeon()) {
            bossActive = false;
        }
    }

    public boolean isBossActive() { return bossActive; }
    public String  getCurrentBoss() { return currentBoss; }
    public int     getPhase() { return phase; }

    /** Short HUD label, or "" when no boss is active. */
    public String getBossLabel() {
        if (!bossActive || currentBoss.isEmpty()) return "";
        return phase > 0 ? currentBoss + " (P" + phase + ")" : currentBoss;
    }

    public void onNewRun() {
        currentBoss = "";
        phase = 0;
        bossActive = false;
    }
}
