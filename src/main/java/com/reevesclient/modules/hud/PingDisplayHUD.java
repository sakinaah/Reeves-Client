package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplayHUD extends HUDElement {

    private int ping = 0;

    public PingDisplayHUD() {
        super("ping_display", "Ping Display", 0.01f, 0.07f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        if (entry != null) ping = entry.getLatency();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        int color = pingColor();
        RenderUtil.drawText(ctx, ping + " ms", 0, 0, applyOpacity(color));
    }

    private int pingColor() {
        if (ping < 80)  return 0xFF4CAF50;
        if (ping < 150) return getTextColor();
        if (ping < 250) return 0xFFFF9800;
        return 0xFFE53935;
    }

    @Override public int getWidth()  { return 50; }
    @Override public int getHeight() { return 9; }
}
