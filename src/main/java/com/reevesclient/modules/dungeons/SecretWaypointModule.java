package com.reevesclient.modules.dungeons;

import com.google.gson.*;
import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Shows 3D waypoints at known secret locations in dungeon rooms.
 *
 * SAFETY DESIGN:
 * - All waypoint coordinates are from publicly known community data.
 * - The player must walk to each waypoint and interact themselves.
 * - No secrets are auto-collected, no items are auto-clicked.
 * - No movement assistance of any kind.
 * - Equivalent to overlays in NEU, SkyHanni, and Skytils — widely community-accepted.
 *
 * Data source: bundled rooms.json with known secret offsets per room.
 */
public class SecretWaypointModule extends Module {

    public enum SecretType { CHEST, BAT, WITHER, ITEM, FAIRY, ESSENCE }

    public record SecretWaypoint(
        String     roomId,
        String     label,
        SecretType type,
        int        offsetX,   // block offset from room origin
        int        offsetY,
        int        offsetZ
    ) {}

    private final Map<String, List<SecretWaypoint>> waypointsByRoom = new HashMap<>();
    private final List<SecretWaypoint> activeWaypoints = new ArrayList<>();

    private final BooleanSetting showChests;
    private final BooleanSetting showBats;
    private final BooleanSetting showFairy;
    private final BooleanSetting showCompleted;

    // Track which secrets the player has already found this run
    private final Set<String> completedSecrets = new HashSet<>();

    public SecretWaypointModule() {
        super("dungeon_secrets", "Secret Waypoints",
              "Shows waypoints for known secret locations. Player must interact with all secrets manually.",
              ModuleCategory.DUNGEONS, true);

        showChests    = addSetting(new BooleanSetting("show_chests",  "Show Chests",    "Display chest secret waypoints.", true));
        showBats      = addSetting(new BooleanSetting("show_bats",    "Show Bats",      "Display bat secret waypoints.", true));
        showFairy     = addSetting(new BooleanSetting("show_fairy",   "Show Fairy",     "Display fairy soul waypoints.", true));
        showCompleted = addSetting(new BooleanSetting("show_completed","Show Completed", "Keep waypoints visible after finding them.", false));

        loadWaypointData();
    }

    private void loadWaypointData() {
        try (InputStream is = getClass().getResourceAsStream(
                "/data/reeves-client/dungeons/secrets.json")) {
            if (is == null) return;
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                                      .getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o   = el.getAsJsonObject();
                String roomId  = o.get("room").getAsString();
                String label   = o.get("label").getAsString();
                SecretType type = SecretType.valueOf(o.get("type").getAsString().toUpperCase());
                int ox = o.get("x").getAsInt();
                int oy = o.get("y").getAsInt();
                int oz = o.get("z").getAsInt();
                waypointsByRoom.computeIfAbsent(roomId, k -> new ArrayList<>())
                               .add(new SecretWaypoint(roomId, label, type, ox, oy, oz));
            }
            ReevesClient.LOGGER.info("Loaded secret waypoints for {} rooms.", waypointsByRoom.size());
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load secret waypoints: {}", e.getMessage());
        }
    }

    /** Updates active waypoints based on the currently detected room. */
    public void updateForRoom(String roomId, BlockPos roomOrigin) {
        activeWaypoints.clear();
        if (roomId == null || roomOrigin == null) return;
        List<SecretWaypoint> wps = waypointsByRoom.get(roomId);
        if (wps != null) activeWaypoints.addAll(wps);
    }

    /** Called when the player collects a secret (detected via chat message). */
    public void markCompleted(String secretKey) {
        completedSecrets.add(secretKey);
    }

    /** Resets per-run state. Call when entering a new dungeon. */
    public void onNewRun() {
        completedSecrets.clear();
        activeWaypoints.clear();
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !HypixelUtil.isInDungeon()) {
            activeWaypoints.clear();
        }
    }

    /** Returns waypoints that should be rendered this frame. */
    public List<SecretWaypoint> getVisibleWaypoints(BlockPos roomOrigin) {
        if (!isEnabled() || roomOrigin == null) return Collections.emptyList();
        List<SecretWaypoint> visible = new ArrayList<>();
        for (SecretWaypoint wp : activeWaypoints) {
            if (isFiltered(wp)) continue;
            String key = wp.roomId() + ":" + wp.label();
            if (!showCompleted.getValue() && completedSecrets.contains(key)) continue;
            visible.add(wp);
        }
        return visible;
    }

    private boolean isFiltered(SecretWaypoint wp) {
        return switch (wp.type()) {
            case CHEST -> !showChests.getValue();
            case BAT   -> !showBats.getValue();
            case FAIRY -> !showFairy.getValue();
            default    -> false;
        };
    }

    /** Converts a room-relative waypoint to absolute world coordinates. */
    public static BlockPos toWorldPos(SecretWaypoint wp, BlockPos roomOrigin) {
        return roomOrigin.add(wp.offsetX(), wp.offsetY(), wp.offsetZ());
    }
}
