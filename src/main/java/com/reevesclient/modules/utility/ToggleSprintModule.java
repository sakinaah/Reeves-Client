package com.reevesclient.modules.utility;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Holds the sprint key pressed so the player does not have to hold it.
 * This is a purely client-side QoL feature universally allowed by Hypixel.
 * It does NOT send any extra sprint packets; it simply keeps the key-binding pressed.
 */
public class ToggleSprintModule extends Module {

    private boolean toggled = false;

    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint",
              "Automatically holds sprint so you don't need to hold the key.",
              ModuleCategory.UTILITY, false);
    }

    /** Called from MixinClientPlayerEntity to check if sprint should be held. */
    public boolean isSprinting() {
        return isEnabled() && toggled;
    }

    /** Called when the sprint key is pressed — toggles the sprint state. */
    public void onSprintKeyPress() {
        if (isEnabled()) toggled = !toggled;
    }

    @Override
    protected void onDisable() {
        toggled = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (isEnabled() && toggled) {
            client.options.sprintKey.setPressed(true);
        }
    }
}
