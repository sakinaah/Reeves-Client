package com.reevesclient.ui.components;

import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** Horizontal slider with label and live value display. */
public class RSlider extends ClickableWidget {

    private float value;
    private final float min, max;
    private final String label;
    private final Consumer<Float> onChange;
    private boolean dragging = false;

    public RSlider(int x, int y, int w, String label, float value, float min, float max,
                   Consumer<Float> onChange) {
        super(x, y, w, 18, Text.empty());
        this.label    = label;
        this.value    = value;
        this.min      = min;
        this.max      = max;
        this.onChange = onChange;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x = getX(); int y = getY(); int w = getWidth();
        float pct = (value - min) / (max - min);

        // Track
        ctx.fill(x, y + 7, x + w, y + 11, 0xFF444444);
        // Fill
        ctx.fill(x, y + 7, x + (int)(pct * w), y + 11, ColorUtil.RC_ACCENT);
        // Thumb
        int tx = x + (int)(pct * w) - 5;
        RenderUtil.fillRoundedRect(ctx, tx, y + 4, 10, 10, 5, 0xFFFFFFFF);
        // Label + value
        String display = label + ": " + String.format("%.1f", value);
        RenderUtil.drawText(ctx, display, x, y - 2, ColorUtil.RC_TEXT);
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        if (isMouseOver(click.x(), click.y())) { dragging = true; updateFromMouse(click.x()); return true; }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging) { updateFromMouse(click.x()); return true; }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = false; return super.mouseReleased(click);
    }

    private void updateFromMouse(double mx) {
        float pct = (float) ((mx - getX()) / getWidth());
        value = min + Math.max(0f, Math.min(1f, pct)) * (max - min);
        if (onChange != null) onChange.accept(value);
    }

    @Override
    public void onClick(Click click, boolean shifted) { updateFromMouse(click.x()); }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public float getValue() { return value; }
    public void  setValue(float v) { this.value = Math.max(min, Math.min(max, v)); }
}
