package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FPSCounterHUD extends HUDElement {

    private int fps = 0;

    public FPSCounterHUD() {
        super("fps_counter", "FPS Counter", 0.01f, 0.01f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        fps = client.getCurrentFps();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        int color = fpsColor();
        String text = fps + " FPS";
        RenderUtil.drawText(ctx, text, 0, 0, applyOpacity(color));
    }

    private int fpsColor() {
        if (fps >= 120) return 0xFF4CAF50;
        if (fps >= 60)  return getTextColor();
        if (fps >= 30)  return 0xFFFF9800;
        return 0xFFE53935;
    }

    @Override public int getWidth()  { return 50; }
    @Override public int getHeight() { return 9; }
}
