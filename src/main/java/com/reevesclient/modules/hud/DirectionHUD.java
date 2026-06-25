package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class DirectionHUD extends HUDElement {

    private String facing     = "?";
    private float  yaw        = 0f;
    private int    chunkX     = 0;
    private int    chunkZ     = 0;

    public DirectionHUD() {
        super("direction", "Direction", 0.01f, 0.13f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        yaw = MathHelper.wrapDegrees(client.player.getYaw());
        facing = directionFromYaw(yaw);
        chunkX = (int) client.player.getX() >> 4;
        chunkZ = (int) client.player.getZ() >> 4;
    }

    private String directionFromYaw(float yaw) {
        if (yaw < -157.5 || yaw >= 157.5)  return "South";
        if (yaw < -112.5) return "Southeast";
        if (yaw < -67.5)  return "East";
        if (yaw < -22.5)  return "Northeast";
        if (yaw < 22.5)   return "North";
        if (yaw < 67.5)   return "Northwest";
        if (yaw < 112.5)  return "West";
        return "Southwest";
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        int text = themedText(ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, "Facing: " + facing, 0, 0, text);
        RenderUtil.drawText(ctx, String.format("Yaw: %.1f°", yaw), 0, 10, text);
        RenderUtil.drawText(ctx, "Chunk: " + chunkX + ", " + chunkZ, 0, 20, text);
    }

    @Override public int getWidth()  { return 110; }
    @Override public int getHeight() { return 28; }
}
