package com.reevesclient.modules.pvp;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders a fully customizable crosshair in place of the vanilla one.
 * No aim assistance whatsoever — cosmetic/visual only.
 */
public class CrosshairModule extends Module {

    public enum CrosshairStyle { DEFAULT, CROSS, DOT, CIRCLE, CROSS_DOT }

    private final EnumSetting<CrosshairStyle> style;
    private final IntSetting     size;
    private final IntSetting     gap;
    private final IntSetting     thickness;
    private final ColorSetting   color;
    private final BooleanSetting showDot;
    private final BooleanSetting hideVanilla;

    public static boolean shouldHideVanilla = false;

    public CrosshairModule() {
        super("crosshair", "Custom Crosshair",
              "Customize crosshair style, size, color, and gap. No aim assistance.",
              ModuleCategory.PVP, false);

        style       = addSetting(new EnumSetting<>("style", "Style", "Crosshair shape.",
                                  CrosshairStyle.CROSS, CrosshairStyle.class));
        size        = addSetting(new IntSetting("size",      "Size",      "Total size in pixels.", 10, 4, 40));
        gap         = addSetting(new IntSetting("gap",       "Gap",       "Center gap size.", 3, 0, 20));
        thickness   = addSetting(new IntSetting("thickness", "Thickness", "Line thickness.", 1, 1, 5));
        color       = addSetting(new ColorSetting("color", "Color", "Crosshair color (ARGB).", 0xFFFFFFFF));
        showDot     = addSetting(new BooleanSetting("dot", "Show Center Dot", "Add a dot at the crosshair center.", false));
        hideVanilla = addSetting(new BooleanSetting("hide_vanilla", "Hide Vanilla Crosshair",
                                  "Replace vanilla crosshair entirely.", true));
    }

    @Override
    protected void onEnable()  { shouldHideVanilla = hideVanilla.getValue(); }
    @Override
    protected void onDisable() { shouldHideVanilla = false; }

    @Override
    public void onTick(net.minecraft.client.MinecraftClient client) {
        shouldHideVanilla = isEnabled() && hideVanilla.getValue();
    }

    /** Called from MixinInGameHud.renderCrosshair to render the custom crosshair. */
    public void render(DrawContext ctx) {
        if (!isEnabled()) return;
        int sw = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledWidth();
        int sh = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        int c  = color.getValue();
        int s  = size.getValue();
        int g  = gap.getValue();
        int t  = thickness.getValue();

        switch (style.getValue()) {
            case CROSS, CROSS_DOT -> {
                // Horizontal
                ctx.fill(cx - s, cy - t/2, cx - g, cy + t - t/2, c);
                ctx.fill(cx + g, cy - t/2, cx + s, cy + t - t/2, c);
                // Vertical
                ctx.fill(cx - t/2, cy - s, cx + t - t/2, cy - g, c);
                ctx.fill(cx - t/2, cy + g, cx + t - t/2, cy + s, c);
            }
            case DOT -> {
                ctx.fill(cx - t, cy - t, cx + t, cy + t, c);
            }
            case CIRCLE -> {
                drawCircleOutline(ctx, cx, cy, s, t, c);
            }
            default -> {}
        }

        if (showDot.getValue() || style.getValue() == CrosshairStyle.CROSS_DOT) {
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        }
    }

    private void drawCircleOutline(DrawContext ctx, int cx, int cy, int r, int thick, int color) {
        for (int angle = 0; angle < 360; angle++) {
            double rad = Math.toRadians(angle);
            for (int d = 0; d < thick; d++) {
                int x = (int) Math.round(cx + (r - d) * Math.cos(rad));
                int y = (int) Math.round(cy + (r - d) * Math.sin(rad));
                ctx.fill(x, y, x + 1, y + 1, color);
            }
        }
    }
}
