package com.reevesclient.core.module;

import com.google.gson.JsonObject;
import com.reevesclient.core.module.settings.ModuleSetting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for every Reeves Client feature module.
 *
 * Lifecycle:
 *  onEnable()  → called when the module is turned on
 *  onDisable() → called when the module is turned off
 *  onTick()    → called every client tick (only when enabled)
 */
public abstract class Module {

    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;

    private boolean enabled;

    protected final List<ModuleSetting<?>> settings = new ArrayList<>();

    protected Module(String id, String name, String description,
                     ModuleCategory category, boolean defaultEnabled) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.enabled     = defaultEnabled;
        registerSettings();
    }

    // ── Lifecycle hooks (override as needed) ────────────────────────────────

    /** Called once when the module system starts up. */
    public void init() {}

    /** Override to declare settings via addSetting(). Called in constructor. */
    protected void registerSettings() {}

    /** Called every client tick when the module is enabled. */
    public void onTick(MinecraftClient client) {}

    /** Called when this module is enabled by the user. */
    protected void onEnable() {}

    /** Called when this module is disabled by the user. */
    protected void onDisable() {}

    // ── Enable / Disable ────────────────────────────────────────────────────

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    // ── Settings ────────────────────────────────────────────────────────────

    protected <T extends ModuleSetting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<ModuleSetting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public ModuleSetting<?> getSetting(String key) {
        return settings.stream().filter(s -> s.getKey().equals(key)).findFirst().orElse(null);
    }

    // ── Serialization ───────────────────────────────────────────────────────

    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", enabled);
        JsonObject settingsObj = new JsonObject();
        for (ModuleSetting<?> s : settings) {
            settingsObj.add(s.getKey(), s.serialize());
        }
        obj.add("settings", settingsObj);
        return obj;
    }

    public void deserialize(JsonObject obj) {
        if (obj.has("enabled")) {
            enabled = obj.get("enabled").getAsBoolean();
        }
        if (obj.has("settings")) {
            JsonObject settingsObj = obj.getAsJsonObject("settings");
            for (ModuleSetting<?> s : settings) {
                if (settingsObj.has(s.getKey())) {
                    try {
                        s.deserialize(settingsObj.get(s.getKey()));
                    } catch (Exception e) {
                        com.reevesclient.ReevesClient.LOGGER.warn(
                                "Failed to deserialize setting '{}' in module '{}': {}",
                                s.getKey(), id, e.getMessage());
                    }
                }
            }
        }
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    public String          getId()          { return id; }
    public String          getName()        { return name; }
    public String          getDescription() { return description; }
    public ModuleCategory  getCategory()    { return category; }
}
