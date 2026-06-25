package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

/** Real-time scrolling FPS + frametime graph. */
public class FPSGraphHUD extends HUDElement {

    private static final int SAMPLES     = 100;
    private static final int GRAPH_W     = 120;
    private static final int GRAPH_H     = 40;
    private static final int TARGET_FPS  = 60;

    private final Deque<Integer> fpsSamples = new ArrayDeque<>(SAMPLES);

    public FPSGraphHUD() {
        super("fps_graph", "FPS Graph", 0.01f, 0.04f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        fpsSamples.addLast(client.getCurrentFps());
        while (fpsSamples.size() > SAMPLES) fpsSamples.pollFirst();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        // Background
        RenderUtil.fillRect(ctx, 0, 0, GRAPH_W, GRAPH_H + 12, ColorUtil.RC_BG);

        if (fpsSamples.isEmpty()) return;

        // Determine peak FPS for scaling
        int peak = fpsSamples.stream().mapToInt(Integer::intValue).max().orElse(TARGET_FPS);
        int scale = Math.max(peak, TARGET_FPS);

        // Draw bars
        int[] arr  = fpsSamples.stream().mapToInt(Integer::intValue).toArray();
        float barW = (float) GRAPH_W / SAMPLES;
        for (int i = 0; i < arr.length; i++) {
            int barH = (int) ((float) arr[i] / scale * GRAPH_H);
            int color = arr[i] >= TARGET_FPS ? ColorUtil.RC_SUCCESS : ColorUtil.RC_WARNING;
            int bx = (int) (i * barW);
            RenderUtil.fillRect(ctx, bx, GRAPH_H - barH, (int) Math.ceil(barW), barH, applyOpacity(color));
        }

        // Target FPS line
        int targetY = GRAPH_H - (int) ((float) TARGET_FPS / scale * GRAPH_H);
        RenderUtil.fillRect(ctx, 0, targetY, GRAPH_W, 1, applyOpacity(0x99FFFFFF));

        // Label
        int current = arr[arr.length - 1];
        RenderUtil.drawText(ctx, current + " FPS", 2, GRAPH_H + 2, applyOpacity(ColorUtil.RC_TEXT));
    }

    @Override public int getWidth()  { return GRAPH_W; }
    @Override public int getHeight() { return GRAPH_H + 12; }
}
