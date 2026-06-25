package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;

public class WeatherHUD extends HUDElement {

    private String weather = "Clear";
    private int    weatherColor = ColorUtil.RC_SUCCESS;

    public WeatherHUD() {
        super("weather", "Weather", 0.01f, 0.22f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) { weather = "Unknown"; weatherColor = ColorUtil.RC_TEXT_MUTED; return; }

        if (world.isThundering()) {
            weather = "Thunder";
            weatherColor = ColorUtil.RC_ERROR;
        } else if (world.isRaining()) {
            weather = "Rain";
            weatherColor = 0xFF82B1FF;
        } else {
            weather = "Clear";
            weatherColor = ColorUtil.RC_SUCCESS;
        }
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        RenderUtil.drawText(ctx, "Weather: " + weather, 0, 0, applyOpacity(weatherColor));
    }

    @Override public int getWidth()  { return 90; }
    @Override public int getHeight() { return 9; }
}
