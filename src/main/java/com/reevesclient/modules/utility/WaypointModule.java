package com.reevesclient.modules.utility;

import com.google.gson.*;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.ColorUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manual-placement waypoint system.
 * Renders colored beacons + name labels in world-space.
 * All waypoints are placed explicitly by the player — no automation.
 */
public class WaypointModule extends Module {

    public static final class Waypoint {
        public String name;
        public double x, y, z;
        public int    color;
        public boolean visible;

        public Waypoint(String name, double x, double y, double z, int color) {
            this.name    = name;
            this.x       = x;
            this.y       = y;
            this.z       = z;
            this.color   = color;
            this.visible = true;
        }
    }

    private final List<Waypoint> waypoints = new ArrayList<>();
    private final Path saveFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public WaypointModule() {
        super("waypoints", "Waypoints",
              "Place custom 3D waypoints in the world.",
              ModuleCategory.UTILITY, true);
        saveFile = FabricLoader.getInstance().getConfigDir()
                               .resolve("reeves-client/waypoints.json");
        load();
    }

    public List<Waypoint> getWaypoints() { return Collections.unmodifiableList(waypoints); }

    public Waypoint addWaypoint(String name, double x, double y, double z, int color) {
        Waypoint wp = new Waypoint(name, x, y, z, color);
        waypoints.add(wp);
        save();
        return wp;
    }

    /** Adds a waypoint at the player's current position. */
    public Waypoint addAtCurrentPosition(String name, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;
        return addWaypoint(name, mc.player.getX(), mc.player.getY(), mc.player.getZ(), color);
    }

    public void removeWaypoint(Waypoint wp) {
        waypoints.remove(wp);
        save();
    }

    public void clearAll() {
        waypoints.clear();
        save();
    }

    // ── Serialization ────────────────────────────────────────────────────────

    private void save() {
        try {
            Files.createDirectories(saveFile.getParent());
            JsonArray arr = new JsonArray();
            for (Waypoint wp : waypoints) {
                JsonObject o = new JsonObject();
                o.addProperty("name",    wp.name);
                o.addProperty("x",       wp.x);
                o.addProperty("y",       wp.y);
                o.addProperty("z",       wp.z);
                o.addProperty("color",   wp.color);
                o.addProperty("visible", wp.visible);
                arr.add(o);
            }
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(saveFile), StandardCharsets.UTF_8)) {
                GSON.toJson(arr, w);
            }
        } catch (IOException e) {
            com.reevesclient.ReevesClient.LOGGER.warn("Failed to save waypoints: {}", e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try (Reader r = new InputStreamReader(Files.newInputStream(saveFile), StandardCharsets.UTF_8)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                Waypoint wp = new Waypoint(
                    o.get("name").getAsString(),
                    o.get("x").getAsDouble(),
                    o.get("y").getAsDouble(),
                    o.get("z").getAsDouble(),
                    o.get("color").getAsInt()
                );
                wp.visible = !o.has("visible") || o.get("visible").getAsBoolean();
                waypoints.add(wp);
            }
        } catch (Exception e) {
            com.reevesclient.ReevesClient.LOGGER.warn("Failed to load waypoints: {}", e.getMessage());
        }
    }
}
