package com.reevesclient.modules.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

public class FPSCounterModule extends Module {

    public FPSCounterModule() {
        super("fps_counter", "FPS Counter",
              "Displays your current frames per second.",
              ModuleCategory.HUD, true);
    }

    @Override
    protected void onEnable() {
        HUDElement el = ReevesClient.getInstance().getHUDManager().getById("fps_counter");
        if (el != null) el.setVisible(true);
    }

    @Override
    protected void onDisable() {
        HUDElement el = ReevesClient.getInstance().getHUDManager().getById("fps_counter");
        if (el != null) el.setVisible(false);
    }
}
