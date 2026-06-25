package com.reevesclient.ui.components;

import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Styled text field matching the Reeves Client design system. */
public class RTextField extends TextFieldWidget {

    private final String placeholder;

    public RTextField(int x, int y, int w, int h, String placeholder) {
        super(MinecraftClient.getInstance().textRenderer, x, y, w, h, Text.empty());
        this.placeholder = placeholder;
        setMaxLength(256);
        setEditableColor(ColorUtil.RC_TEXT);
    }

    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Background
        RenderUtil.fillRoundedRect(ctx, getX(), getY(), getWidth(), getHeight(), 4,
                isFocused() ? 0xFF252545 : 0xFF1A1A30);
        RenderUtil.drawBorder(ctx, getX(), getY(), getWidth(), getHeight(), 1,
                isFocused() ? ColorUtil.RC_ACCENT : ColorUtil.RC_BORDER);

        // Placeholder
        if (getText().isEmpty() && !isFocused() && !placeholder.isEmpty()) {
            RenderUtil.drawText(ctx, placeholder, getX() + 4, getY() + (getHeight() - 8) / 2,
                    ColorUtil.RC_TEXT_MUTED);
        }

        super.renderWidget(ctx, mouseX, mouseY, delta);
    }
}
