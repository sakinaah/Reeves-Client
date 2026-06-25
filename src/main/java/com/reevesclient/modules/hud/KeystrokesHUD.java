package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Classic WASD + Space + Sneak keystroke display. */
public class KeystrokesHUD extends HUDElement {

    private boolean w, a, s, d, space;

    public KeystrokesHUD() {
        super("keystrokes", "Keystrokes", 0.44f, 0.77f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.options == null) return;
        w     = client.options.forwardKey.isPressed();
        a     = client.options.leftKey.isPressed();
        s     = client.options.backKey.isPressed();
        d     = client.options.rightKey.isPressed();
        space = client.options.jumpKey.isPressed();
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        // Layout: 3 rows — top row = [W], middle = [A][S][D], bottom = [SPACE]
        drawKey(ctx, 18, 0,  "W", w);
        drawKey(ctx, 0,  18, "A", a);
        drawKey(ctx, 18, 18, "S", s);
        drawKey(ctx, 36, 18, "D", d);
        drawKey(ctx, 0,  36, 54, "SPACE", space);
    }

    private void drawKey(DrawContext ctx, int x, int y, String label, boolean pressed) {
        drawKey(ctx, x, y, 14, label, pressed);
    }

    private void drawKey(DrawContext ctx, int x, int y, int w, String label, boolean pressed) {
        int bgColor  = pressed ? themedAccent(ColorUtil.RC_ACCENT) : applyOpacity(0x66000000);
        int txtColor = applyOpacity(0xFFFFFFFF);

        RenderUtil.fillRoundedRect(ctx, x, y, w, 14, 3, bgColor);
        int tx = x + (w - RenderUtil.textWidth(label)) / 2;
        int ty = y + (14 - RenderUtil.textHeight()) / 2;
        RenderUtil.drawTextNoShadow(ctx, label, tx, ty, txtColor);
    }

    @Override public int getWidth()  { return 54; }
    @Override public int getHeight() { return 50; }
}
