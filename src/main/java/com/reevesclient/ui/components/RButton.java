package com.reevesclient.ui.components;

import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** Reeves Client styled button — rounded corners, smooth hover tint. */
public class RButton extends ClickableWidget {

    private final Consumer<RButton> onClick;
    private int baseColor   = ColorUtil.RC_SURFACE;
    private int hoverColor  = ColorUtil.RC_ACCENT;
    private int textColor   = ColorUtil.RC_TEXT;

    private float hoverAnim = 0f; // 0=unhovered, 1=hovered

    public RButton(int x, int y, int w, int h, String label, Consumer<RButton> onClick) {
        super(x, y, w, h, Text.literal(label));
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered();
        // Animate hover blend
        float target = hovered ? 1f : 0f;
        hoverAnim += (target - hoverAnim) * 0.25f;

        int bg = ColorUtil.lerp(baseColor, hoverColor, hoverAnim);
        RenderUtil.fillRoundedRect(ctx, getX(), getY(), getWidth(), getHeight(), 5, bg);
        RenderUtil.drawBorder(ctx, getX(), getY(), getWidth(), getHeight(), 1,
                hovered ? ColorUtil.RC_ACCENT : ColorUtil.RC_BORDER);

        String label = getMessage().getString();
        int tx = getX() + (getWidth()  - RenderUtil.textWidth(label)) / 2;
        int ty = getY() + (getHeight() - RenderUtil.textHeight()) / 2;
        RenderUtil.drawText(ctx, label, tx, ty, textColor);
    }

    @Override
    public void onClick(Click click, boolean shifted) {
        if (onClick != null) onClick.accept(this);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    public RButton setColors(int base, int hover, int text) {
        this.baseColor = base; this.hoverColor = hover; this.textColor = text; return this;
    }
}
