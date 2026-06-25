package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CoordinatesHUD extends HUDElement {

    private double x, y, z;

    public CoordinatesHUD() {
        super("coordinates", "Coordinates", 0.01f, 0.09f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        x = client.player.getX();
        y = client.player.getY();
        z = client.player.getZ();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        int text = themedText(0xFFE0E0E0);

        RenderUtil.drawText(ctx, String.format("X: %.1f", x), 0, 0,  text);
        RenderUtil.drawText(ctx, String.format("Y: %.1f", y), 0, 10, text);
        RenderUtil.drawText(ctx, String.format("Z: %.1f", z), 0, 20, text);
    }

    @Override public int getWidth()  { return 80; }
    @Override public int getHeight() { return 28; }
}
