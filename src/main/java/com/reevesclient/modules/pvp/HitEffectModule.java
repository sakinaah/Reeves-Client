package com.reevesclient.modules.pvp;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;

/**
 * Customizes the color that entities flash when hit.
 * Client-side visual change only — no gameplay effect.
 */
public class HitEffectModule extends Module {

    private final ColorSetting hitColor;
    private final BooleanSetting customColor;

    /** Read by MixinEntityRenderer to override the hurt tint. */
    public static int currentHitColor     = 0xFFFF4444;
    public static boolean useCustomColor  = false;

    public HitEffectModule() {
        super("hit_effect", "Hit Effect",
              "Customize the color entities flash when taking damage.",
              ModuleCategory.PVP, false);

        customColor = addSetting(new BooleanSetting(
            "custom_color", "Use Custom Color", "Enable custom hit flash color.", true));

        hitColor = addSetting(new ColorSetting(
            "color", "Hit Color", "ARGB color of the hit flash.", 0xFFFF0000));
    }

    @Override
    protected void onEnable()  { updateStatics(); }
    @Override
    protected void onDisable() { useCustomColor = false; }

    @Override
    public void onTick(net.minecraft.client.MinecraftClient client) {
        updateStatics();
    }

    private void updateStatics() {
        useCustomColor   = isEnabled() && customColor.getValue();
        currentHitColor  = hitColor.getValue();
    }
}
