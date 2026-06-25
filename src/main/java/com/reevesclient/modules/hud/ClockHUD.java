package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.core.util.TimeUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ClockHUD extends HUDElement {

    private String time = "";

    public ClockHUD() {
        super("clock", "Clock", 0.01f, 0.18f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        time = TimeUtil.currentTime24h();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        RenderUtil.drawText(ctx, time, 0, 0, applyOpacity(ColorUtil.RC_TEXT));
    }

    @Override public int getWidth()  { return 40; }
    @Override public int getHeight() { return 9; }
}
