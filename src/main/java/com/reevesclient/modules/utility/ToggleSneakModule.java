package com.reevesclient.modules.utility;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/** Toggles sneaking on a single key press. Client-side QoL. */
public class ToggleSneakModule extends Module {

    private boolean toggled = false;

    public ToggleSneakModule() {
        super("toggle_sneak", "Toggle Sneak",
              "Toggles sneaking with a single key press.",
              ModuleCategory.UTILITY, false);
    }

    public boolean isSneaking() { return isEnabled() && toggled; }

    public void onSneakKeyPress() {
        if (isEnabled()) toggled = !toggled;
    }

    @Override
    protected void onDisable() { toggled = false; }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (isEnabled() && toggled) {
            client.options.sneakKey.setPressed(true);
        }
    }
}
