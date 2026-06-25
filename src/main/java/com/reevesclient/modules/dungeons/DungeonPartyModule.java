package com.reevesclient.modules.dungeons;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.regex.*;

/**
 * Tracks dungeon party member classes and HP from the tab list and chat.
 * Informational only — displays data already visible to the player.
 */
public class DungeonPartyModule extends Module {

    public enum DungeonClass { ARCHER, BERSERK, HEALER, MAGE, TANK, UNKNOWN }

    public static final class PartyMember {
        public String       name;
        public DungeonClass dungeonClass;
        public int          classLevel;
        public double       hp;
        public double       maxHp;
        public boolean      isDead;

        public PartyMember(String name) {
            this.name         = name;
            this.dungeonClass = DungeonClass.UNKNOWN;
            this.classLevel   = 0;
        }
    }

    // Pattern matching tab-list format: "[M] PlayerName [ARCHER lvl 22]"
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "(ARCHER|BERSERK|HEALER|MAGE|TANK)\\s+lvl\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

    private final Map<String, PartyMember> members = new LinkedHashMap<>();

    public DungeonPartyModule() {
        super("dungeon_party", "Dungeon Party Stats",
              "Shows party member class and performance in the current dungeon run.",
              ModuleCategory.DUNGEONS, true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInDungeon() || client.getNetworkHandler() == null) return;
        members.clear();
        // Parse tab-list entries
        for (var entry : client.getNetworkHandler().getPlayerList()) {
            if (entry.getDisplayName() == null) continue;
            String display = com.reevesclient.core.util.TextUtil.stripFormatting(
                    entry.getDisplayName().getString());
            Matcher m = CLASS_PATTERN.matcher(display);
            String playerName = entry.getProfile().name();
            PartyMember member = members.computeIfAbsent(playerName, PartyMember::new);
            if (m.find()) {
                member.dungeonClass = DungeonClass.valueOf(m.group(1).toUpperCase());
                member.classLevel   = Integer.parseInt(m.group(2));
            }
        }
    }

    public Map<String, PartyMember> getMembers() { return Collections.unmodifiableMap(members); }

    public void markDead(String playerName) {
        PartyMember m = members.get(playerName);
        if (m != null) m.isDead = true;
    }

    public void markRevived(String playerName) {
        PartyMember m = members.get(playerName);
        if (m != null) m.isDead = false;
    }
}
