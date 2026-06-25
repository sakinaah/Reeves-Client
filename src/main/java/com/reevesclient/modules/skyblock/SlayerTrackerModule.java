package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

import java.util.*;
import java.util.regex.*;

/**
 * Tracks slayer XP and boss kill counts by reading chat messages.
 * Uses only data already visible to the player in public chat.
 */
public class SlayerTrackerModule extends Module {

    public static final class SlayerData {
        public String name;
        public int    level;
        public long   xp;
        public int    bossKills;
        public int    tier1, tier2, tier3, tier4, tier5;

        public SlayerData(String name) { this.name = name; }
    }

    // XP thresholds per level for each slayer type
    private static final long[] SLAYER_XP = { 0, 5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000 };

    // Pattern: "ZOMBIE SLAYER LVL 3 COMPLETE! +100 XP"
    private static final Pattern COMPLETE_PATTERN = Pattern.compile(
        "(ZOMBIE|SPIDER|WOLF|ENDERMAN|BLAZE|VAMPIRE)\\s+SLAYER.*?\\+(\\d+)\\s+XP", Pattern.CASE_INSENSITIVE);

    private final Map<String, SlayerData> slayers = new LinkedHashMap<>();

    public SlayerTrackerModule() {
        super("slayer_tracker", "Slayer Tracker",
              "Tracks slayer XP, boss kills, and quest progress from visible chat.",
              ModuleCategory.SKYBLOCK, true);
        for (String s : List.of("Zombie", "Spider", "Wolf", "Enderman", "Blaze", "Vampire")) {
            slayers.put(s, new SlayerData(s));
        }
    }

    /** Call from MixinChatScreen or ChatReceiveEvent when a chat line is received. */
    public void onChat(String plain) {
        if (!isEnabled()) return;
        Matcher m = COMPLETE_PATTERN.matcher(plain);
        if (!m.find()) return;
        String type = capitalize(m.group(1));
        long gainedXP = Long.parseLong(m.group(2));
        SlayerData data = slayers.computeIfAbsent(type, SlayerData::new);
        data.xp += gainedXP;
        data.bossKills++;
        data.level = xpToLevel(data.xp);
    }

    private int xpToLevel(long xp) {
        for (int i = SLAYER_XP.length - 1; i >= 0; i--) {
            if (xp >= SLAYER_XP[i]) return i;
        }
        return 0;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public Map<String, SlayerData> getSlayers() { return Collections.unmodifiableMap(slayers); }
}
