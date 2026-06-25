package com.reevesclient.modules.performance;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticlesMode;

/**
 * Overrides Minecraft's particle level setting without losing the player's original preference.
 */
public class ParticleReducerModule extends Module {

    public enum ParticleLevel { ALL, DECREASED, MINIMAL, NONE }

    private final EnumSetting<ParticleLevel> particleLevel;

    private ParticlesMode savedMode = null;

    public ParticleReducerModule() {
        super("particle_reducer", "Particle Reducer",
              "Reduce or hide particle effects to improve performance.",
              ModuleCategory.PERFORMANCE, false);

        particleLevel = addSetting(new EnumSetting<>(
            "level", "Particle Level",
            "How many particles to show.",
            ParticleLevel.DECREASED, ParticleLevel.class));
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;
        savedMode = mc.options.getParticles().getValue();
        applyLevel(mc);
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null || savedMode == null) return;
        mc.options.getParticles().setValue(savedMode);
        savedMode = null;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.options == null) return;
        applyLevel(client);
    }

    private void applyLevel(MinecraftClient mc) {
        ParticlesMode mode = switch (particleLevel.getValue()) {
            case ALL       -> ParticlesMode.ALL;
            case DECREASED -> ParticlesMode.DECREASED;
            case MINIMAL   -> ParticlesMode.MINIMAL;
            case NONE      -> ParticlesMode.MINIMAL; // closest vanilla option
        };
        mc.options.getParticles().setValue(mode);
    }
}
