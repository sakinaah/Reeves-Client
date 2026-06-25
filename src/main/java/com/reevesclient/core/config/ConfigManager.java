package com.reevesclient.core.config;

import com.google.gson.*;
import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.hud.HUDManager;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Loads and saves all configuration as JSON under .minecraft/config/reeves-client/. */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configDir;
    private final Path modulesFile;
    private final Path hudFile;
    private final Path generalFile;

    private JsonObject generalConfig = new JsonObject();

    public ConfigManager() {
        configDir   = FabricLoader.getInstance().getConfigDir().resolve("reeves-client");
        modulesFile = configDir.resolve("modules.json");
        hudFile     = configDir.resolve("hud.json");
        generalFile = configDir.resolve("general.json");
    }

    public void load() {
        try { Files.createDirectories(configDir); }
        catch (IOException e) {
            ReevesClient.LOGGER.error("Failed to create config directory: {}", e.getMessage());
            return;
        }
        generalConfig = readJson(generalFile);
    }

    public void save() {
        try { Files.createDirectories(configDir); }
        catch (IOException e) {
            ReevesClient.LOGGER.error("Failed to create config directory: {}", e.getMessage());
            return;
        }
        writeJson(generalFile, generalConfig);

        ModuleManager mm = ReevesClient.getInstance().getModuleManager();
        if (mm != null) saveModules(mm);

        HUDManager hm = ReevesClient.getInstance().getHUDManager();
        if (hm != null) saveHUD(hm);
    }

    // ── Modules ─────────────────────────────────────────────────────────────

    public void loadModules(ModuleManager manager) {
        JsonObject root = readJson(modulesFile);
        for (Module m : manager.getAll()) {
            if (root.has(m.getId())) {
                try { m.deserialize(root.getAsJsonObject(m.getId())); }
                catch (Exception e) {
                    ReevesClient.LOGGER.warn("Failed to load config for module '{}': {}", m.getId(), e.getMessage());
                }
            }
        }
    }

    private void saveModules(ModuleManager manager) {
        JsonObject root = new JsonObject();
        for (Module m : manager.getAll()) {
            root.add(m.getId(), m.serialize());
        }
        writeJson(modulesFile, root);
    }

    // ── HUD ─────────────────────────────────────────────────────────────────

    public void loadHUD(HUDManager manager) {
        JsonObject root = readJson(hudFile);
        for (HUDElement el : manager.getElements()) {
            if (root.has(el.getId())) {
                el.deserialize(root.getAsJsonObject(el.getId()));
            }
        }
    }

    private void saveHUD(HUDManager manager) {
        JsonObject root = new JsonObject();
        for (HUDElement el : manager.getElements()) {
            root.add(el.getId(), el.serialize());
        }
        writeJson(hudFile, root);
    }

    // ── General settings ────────────────────────────────────────────────────

    public String getString(String key, String def) {
        return generalConfig.has(key) ? generalConfig.get(key).getAsString() : def;
    }

    public boolean getBoolean(String key, boolean def) {
        return generalConfig.has(key) ? generalConfig.get(key).getAsBoolean() : def;
    }

    public int getInt(String key, int def) {
        return generalConfig.has(key) ? generalConfig.get(key).getAsInt() : def;
    }

    public void setString(String key, String value)   { generalConfig.addProperty(key, value); }
    public void setBoolean(String key, boolean value) { generalConfig.addProperty(key, value); }
    public void setInt(String key, int value)         { generalConfig.addProperty(key, value); }

    // ── I/O helpers ─────────────────────────────────────────────────────────

    private JsonObject readJson(Path path) {
        if (!Files.exists(path)) return new JsonObject();
        try (Reader r = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            JsonElement el = JsonParser.parseReader(r);
            return el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to read {}: {}", path.getFileName(), e.getMessage());
            return new JsonObject();
        }
    }

    private void writeJson(Path path, JsonObject obj) {
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
            GSON.toJson(obj, w);
        } catch (IOException e) {
            ReevesClient.LOGGER.error("Failed to write {}: {}", path.getFileName(), e.getMessage());
        }
    }
}
