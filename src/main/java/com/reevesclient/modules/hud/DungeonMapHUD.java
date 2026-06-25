package com.reevesclient.modules.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.module.ModuleManager;
import com.reevesclient.core.util.HypixelUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.modules.dungeons.DungeonMapModule;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

/**
 * Draggable overlay that draws the dungeon map produced by {@link DungeonMapModule}.
 * Renders nothing unless the module is on and a dungeon map is present.
 */
public class DungeonMapHUD extends HUDElement {

    public DungeonMapHUD() {
        super("dungeon_map", "Dungeon Map", 0.80f, 0.05f);
    }

    private DungeonMapModule module() {
        ReevesClient rc = ReevesClient.getInstance();
        if (rc == null) return null;
        ModuleManager mm = rc.getModuleManager();
        return mm == null ? null : mm.get(DungeonMapModule.class);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        DungeonMapModule m = module();
        if (m == null || !m.isEnabled() || !m.hasMap() || !HypixelUtil.isInDungeon()) return;

        int dim = m.getMapDim();
        if (isShowBackground()) {
            RenderUtil.fillRoundedRect(ctx, -2, -2, dim + 4, dim + 4, 3, themedBackground(0xCC101020));
        }
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, m.getTextureId(),
                0, 0, 0.0F, 0.0F, dim, dim, dim, dim);
    }

    @Override public int getWidth()  { return 128; }
    @Override public int getHeight() { return 128; }
}
