package com.reevesclient.modules.performance;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GraphicsMode;

/**
 * Applies preset combinations of vanilla Minecraft graphics settings
 * for quick performance tuning. Does not touch any non-standard settings.
 */
public class PerformanceProfileModule extends Module {

    public enum Profile { NONE, OPTIMIZED, BALANCED, HIGH_QUALITY }

    private final EnumSetting<Profile> profile;

    // Apply a profile only when it changes — not every tick — so the user can
    // still adjust vanilla settings afterwards and we don't burn work each tick.
    private Profile lastApplied = null;

    public PerformanceProfileModule() {
        super("performance_profiles", "Performance Profiles",
              "Quickly apply preset optimization configurations.",
              ModuleCategory.PERFORMANCE, false);

        profile = addSetting(new EnumSetting<>(
            "profile", "Active Profile",
            "The performance profile to apply.",
            Profile.NONE, Profile.class));
    }

    @Override
    protected void onDisable() {
        // Allow re-applying the same profile after a disable/enable cycle.
        lastApplied = null;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.options == null) return;
        Profile current = profile.getValue();
        if (current == lastApplied) return;   // already applied; don't fight the user
        lastApplied = current;
        if (current != Profile.NONE) applyProfile(mc, current);
    }

    private void applyProfile(MinecraftClient mc, Profile p) {
        switch (p) {
            case OPTIMIZED -> {
                mc.options.getGraphicsMode().setValue(GraphicsMode.FAST);
                mc.options.getViewDistance().setValue(6);
                mc.options.getEntityDistanceScaling().setValue(0.5);
                mc.options.getMipmapLevels().setValue(0);
                mc.options.getEntityShadows().setValue(false);
            }
            case BALANCED -> {
                mc.options.getGraphicsMode().setValue(GraphicsMode.FAST);
                mc.options.getViewDistance().setValue(10);
                mc.options.getEntityDistanceScaling().setValue(0.75);
                mc.options.getMipmapLevels().setValue(2);
                mc.options.getEntityShadows().setValue(false);
            }
            case HIGH_QUALITY -> {
                mc.options.getGraphicsMode().setValue(GraphicsMode.FANCY);
                mc.options.getViewDistance().setValue(16);
                mc.options.getEntityDistanceScaling().setValue(1.0);
                mc.options.getMipmapLevels().setValue(4);
                mc.options.getEntityShadows().setValue(true);
            }
            default -> {}
        }
    }
}
