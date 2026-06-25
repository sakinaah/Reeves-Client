package com.reevesclient.core.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Reusable rendering helpers built on top of DrawContext. */
public final class RenderUtil {

    private RenderUtil() {}

    // ── Rectangles ───────────────────────────────────────────────────────────

    public static void fillRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    /** Rounded rectangle approximated with nested fills + corner pixels. */
    public static void fillRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        // Clamp radius
        r = Math.min(r, Math.min(w, h) / 2);
        ctx.fill(x + r,     y,          x + w - r,     y + h,         color); // center vertical band
        ctx.fill(x,         y + r,      x + r,         y + h - r,     color); // left
        ctx.fill(x + w - r, y + r,      x + w,         y + h - r,     color); // right
        // approximate corners with a quarter-circle using small fills
        fillQuarterCircle(ctx, x + r,         y + r,         r, 0, color);
        fillQuarterCircle(ctx, x + w - r - 1, y + r,         r, 1, color);
        fillQuarterCircle(ctx, x + r,         y + h - r - 1, r, 2, color);
        fillQuarterCircle(ctx, x + w - r - 1, y + h - r - 1, r, 3, color);
    }

    private static void fillQuarterCircle(DrawContext ctx, int cx, int cy, int r, int quadrant, int color) {
        for (int dy = 0; dy <= r; dy++) {
            int dx = (int) Math.sqrt((double) r * r - (double) dy * dy);
            int lx = cx - (quadrant == 0 || quadrant == 2 ? dx : 0);
            int rx = cx + (quadrant == 1 || quadrant == 3 ? dx : 0) + 1;
            int ry = cy + (quadrant < 2 ? -dy : dy);
            if (lx < rx) ctx.fill(lx, ry, rx, ry + 1, color);
        }
    }

    /** Outline-only rectangle. */
    public static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int thickness, int color) {
        ctx.fill(x,             y,              x + w,             y + thickness,        color); // top
        ctx.fill(x,             y + h - thickness, x + w,          y + h,               color); // bottom
        ctx.fill(x,             y + thickness,  x + thickness,     y + h - thickness,   color); // left
        ctx.fill(x + w - thickness, y + thickness, x + w,          y + h - thickness,   color); // right
    }

    // ── Text ─────────────────────────────────────────────────────────────────

    public static void drawText(DrawContext ctx, String text, int x, int y, int color) {
        ctx.drawText(mc().textRenderer, text, x, y, color, true);
    }

    public static void drawTextNoShadow(DrawContext ctx, String text, int x, int y, int color) {
        ctx.drawText(mc().textRenderer, text, x, y, color, false);
    }

    public static void drawCenteredText(DrawContext ctx, String text, int cx, int y, int color) {
        TextRenderer tr = mc().textRenderer;
        int w = tr.getWidth(text);
        ctx.drawText(tr, text, cx - w / 2, y, color, true);
    }

    public static int textWidth(String text) {
        return mc().textRenderer.getWidth(text);
    }

    public static int textHeight() {
        return mc().textRenderer.fontHeight;
    }

    // ── Gradient fill ────────────────────────────────────────────────────────

    public static void fillGradientV(DrawContext ctx, int x, int y, int w, int h, int topColor, int bottomColor) {
        // Draw as two triangles using fill with midpoint blending
        int mid = y + h / 2;
        int midColor = ColorUtil.lerp(topColor, bottomColor, 0.5f);
        ctx.fillGradient(x, y,     x + w, mid,    topColor,  midColor);
        ctx.fillGradient(x, mid,   x + w, y + h,  midColor,  bottomColor);
    }

    // ── Tooltip / panel background ───────────────────────────────────────────

    public static void drawPanelBackground(DrawContext ctx, int x, int y, int w, int h) {
        fillRoundedRect(ctx, x, y, w, h, 6, ColorUtil.RC_BG_PANEL);
        drawBorder(ctx, x, y, w, h, 1, ColorUtil.RC_BORDER);
    }

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }
}
