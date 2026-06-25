package com.reevesclient.modules.performance;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;

/**
 * Entity render distance and culling controls.
 * The actual culling is applied in MixinEntityRenderer via the public static field below.
 */
public class EntityRenderModule extends Module {

    /** Read by MixinEntityRenderer to override entity distance culling. */
    public static int entityRenderDistance = 64;
    public static boolean limitInvisibleEntities = false;

    private final IntSetting     renderDistSetting;
    private final BooleanSetting limitInvisible;

    public EntityRenderModule() {
        super("entity_render", "Entity Rendering",
              "Control entity render distance and culling to improve FPS.",
              ModuleCategory.PERFORMANCE, false);

        renderDistSetting = addSetting(new IntSetting(
            "render_distance", "Entity Render Distance",
            "Maximum distance (in blocks) to render entities.",
            64, 8, 128));

        limitInvisible = addSetting(new BooleanSetting(
            "limit_invisible", "Hide Far Invisible Entities",
            "Skip rendering invisible entities beyond a short range.",
            false));
    }

    @Override
    public void onTick(net.minecraft.client.MinecraftClient client) {
        entityRenderDistance      = renderDistSetting.getValue();
        limitInvisibleEntities    = limitInvisible.getValue();
    }
}
