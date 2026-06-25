package com.reevesclient.modules.dungeons;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

/**
 * Estimates the Bazaar value of a dungeon reward chest's <em>visible</em> contents
 * (rendered by the HandledScreen mixin). It only reads items the server has already
 * shown — no hidden-information access — so it is informational and compliant.
 *
 * Chest-title matching REQUIRES RUNTIME VERIFICATION (Hypixel's exact reward-chest
 * titles); {@link #isChestTitle} is deliberately broad and easy to calibrate.
 */
public class DungeonChestValueModule extends Module {

    public DungeonChestValueModule() {
        super("dungeon_chest_value", "Dungeon Chest Value",
                "Estimates the Bazaar value of a dungeon reward chest's contents.",
                ModuleCategory.DUNGEONS, true);
    }

    /** Broad match for dungeon reward-chest GUIs (calibrate against live titles). */
    public boolean isChestTitle(String title) {
        if (title == null) return false;
        String t = title.toLowerCase();
        return t.contains("chest") || t.contains("croesus");
    }
}
