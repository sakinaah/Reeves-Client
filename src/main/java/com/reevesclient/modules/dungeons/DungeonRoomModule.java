package com.reevesclient.modules.dungeons;

import com.google.gson.*;
import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Identifies the current Dungeon room from known community room data.
 * INFORMATIONAL ONLY — displays room name and type.
 * No route planning, no automatic solving, no movement assistance.
 *
 * Room data is loaded from data/reeves-client/dungeons/rooms.json
 * which contains publicly known room layouts compiled by the community.
 */
public class DungeonRoomModule extends Module {

    public enum RoomType { NORMAL, PUZZLE, TRAP, FAIRY, BLOOD, BOSS, SPAWN, UNKNOWN }

    public record RoomData(
        String   id,
        String   name,
        RoomType type,
        String   difficulty, // e.g. "easy", "medium", "hard"
        int      secretCount,
        List<String> notes    // Optional informational notes to show the player
    ) {}

    private final Map<String, RoomData> roomRegistry = new HashMap<>();
    private RoomData currentRoom = null;

    public DungeonRoomModule() {
        super("dungeon_rooms", "Dungeon Room Identifier",
              "Identifies the current dungeon room and displays its name and secret count.",
              ModuleCategory.DUNGEONS, true);
        loadRoomData();
    }

    private void loadRoomData() {
        // Room data bundled with the mod inside the jar
        try (InputStream is = getClass().getResourceAsStream(
                "/data/reeves-client/dungeons/rooms.json")) {
            if (is == null) {
                ReevesClient.LOGGER.warn("Dungeon room data not found.");
                return;
            }
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                      .getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String id   = o.get("id").getAsString();
                String name = o.get("name").getAsString();
                RoomType type = RoomType.valueOf(o.has("type")
                        ? o.get("type").getAsString().toUpperCase() : "NORMAL");
                String difficulty = o.has("difficulty") ? o.get("difficulty").getAsString() : "medium";
                int secrets = o.has("secrets") ? o.get("secrets").getAsInt() : 0;
                List<String> notes = new ArrayList<>();
                if (o.has("notes")) {
                    o.getAsJsonArray("notes").forEach(n -> notes.add(n.getAsString()));
                }
                roomRegistry.put(id, new RoomData(id, name, type, difficulty, secrets, notes));
            }
            ReevesClient.LOGGER.info("Loaded {} dungeon room definitions.", roomRegistry.size());
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load dungeon room data: {}", e.getMessage());
        }
    }

    /**
     * Called each tick when the player is in a dungeon.
     * Detects room by reading a known identifier block at a fixed offset.
     * The detection is heuristic — we match based on recognizable block patterns.
     */
    private int tickCounter = 0;

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInDungeon() || client.player == null) return;
        // Throttle sidebar parsing — room state changes slowly.
        if (tickCounter++ % 10 != 0) return;
        detectRoomFromScoreboard();
    }

    /**
     * Heuristic room-name match against the sidebar. Uses the correct sidebar
     * reader (team prefix/suffix) rather than the raw score-holder name.
     *
     * NOTE: Hypixel does not reliably expose the room name in the sidebar — robust
     * room identification needs dungeon map-item scanning, which is not yet
     * implemented. This remains a best-effort match.
     */
    private void detectRoomFromScoreboard() {
        for (String line : HypixelUtil.getSidebarLines()) {
            if (line.isBlank()) continue;
            String key = line.trim().toLowerCase().replace(" ", "_");
            if (roomRegistry.containsKey(key)) {
                currentRoom = roomRegistry.get(key);
                return;
            }
        }
    }

    public RoomData              getCurrentRoom()   { return currentRoom; }
    public Map<String, RoomData> getRoomRegistry()  { return Collections.unmodifiableMap(roomRegistry); }
}
