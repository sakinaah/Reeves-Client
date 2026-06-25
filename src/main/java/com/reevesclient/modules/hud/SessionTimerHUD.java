package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.core.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SessionTimerHUD extends HUDElement {

    private final long startMs = System.currentTimeMillis();
    private String display = "0:00";

    public SessionTimerHUD() {
        super("session_timer", "Session Timer", 0.01f, 0.2f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        long elapsed = (System.currentTimeMillis() - startMs) / 1000;
        display = TextUtil.formatClock(elapsed);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        RenderUtil.drawText(ctx, "Session: " + display, 0, 0, themedText(ColorUtil.RC_TEXT));
    }

    @Override public int getWidth()  { return 90; }
    @Override public int getHeight() { return 9; }
}
