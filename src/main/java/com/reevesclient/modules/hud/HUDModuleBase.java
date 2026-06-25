package com.reevesclient.modules.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

/**
 * Thin module wrapper that links a named HUD element to the module enable/disable system.
 * Extend this for every HUD element that needs a corresponding settings toggle.
 */
public class HUDModuleBase extends Module {

    private final String hudElementId;

    public HUDModuleBase(String id, String name, String description, boolean defaultEnabled) {
        super(id, name, description, ModuleCategory.HUD, defaultEnabled);
        this.hudElementId = id;
    }

    @Override
    protected void onEnable() {
        var el = ReevesClient.getInstance().getHUDManager().getById(hudElementId);
        if (el != null) el.setVisible(true);
    }

    @Override
    protected void onDisable() {
        var el = ReevesClient.getInstance().getHUDManager().getById(hudElementId);
        if (el != null) el.setVisible(false);
    }

    @Override
    public void init() {
        // Sync initial visibility to enabled state
        var el = ReevesClient.getInstance().getHUDManager().getById(hudElementId);
        if (el != null) el.setVisible(isEnabled());
    }
}
