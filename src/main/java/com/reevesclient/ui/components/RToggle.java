package com.reevesclient.ui.components;

import com.reevesclient.core.util.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** Animated toggle switch. */
public class RToggle extends ClickableWidget {

    private boolean value;
    private float   thumbX; // animated thumb position (0=off, 1=on)
    private final Consumer<Boolean> onChange;

    private static final int W = 36, H = 18;

    public RToggle(int x, int y, boolean initialValue, Consumer<Boolean> onChange) {
        super(x, y, W, H, Text.empty());
        this.value    = initialValue;
        this.thumbX   = initialValue ? 1f : 0f;
        this.onChange = onChange;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Animate thumb
        float target = value ? 1f : 0f;
        thumbX += (target - thumbX) * 0.3f;

        int trackColor = value
            ? ColorUtil.lerp(0xFF444444, ColorUtil.RC_ACCENT, thumbX)
            : 0xFF444444;
        int x = getX(); int y = getY();

        // Track
        ctx.fill(x, y + 4, x + W, y + H - 4, trackColor);
        ctx.fill(x + 4, y, x + W - 4, y + H, trackColor);

        // Thumb circle approximation
        int thumbPixelX = (int) (x + 2 + thumbX * (W - H));
        int ty = y + 2;
        ctx.fill(thumbPixelX, ty, thumbPixelX + H - 4, ty + H - 4, 0xFFFFFFFF);
    }

    @Override
    public void onClick(Click click, boolean shifted) {
        value = !value;
        if (onChange != null) onChange.accept(value);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    public boolean getValue()       { return value; }
    public void    setValue(boolean v) { this.value = v; thumbX = v ? 1f : 0f; }
}
