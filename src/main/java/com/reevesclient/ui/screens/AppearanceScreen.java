package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.theme.ThemeManager;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.RButton;
import com.reevesclient.ui.components.RSlider;
import com.reevesclient.ui.components.RToggle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Unified appearance editor — presets, accent colour, panel opacity, and text
 * shadow. Changes apply live to the whole Reeves UI via {@link ThemeManager}
 * and are saved on exit.
 */
public class AppearanceScreen extends Screen {

    private final Screen parent;
    private ThemeManager theme;

    private static final int PADDING = 12;

    // Captured during init() so the "Text Shadow" label can sit next to its toggle.
    private int shadowLabelX, shadowLabelY;

    public AppearanceScreen(Screen parent) {
        super(Text.literal("Appearance"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        theme = ReevesClient.getInstance().getThemeManager();

        int px = getPanelX(), py = getPanelY(), pw = getPanelW();
        int x = px + PADDING;
        int y = py + 60;

        // Preset buttons (wrap across rows).
        int bx = x, by = y, bw = 96, bh = 22, gap = 6;
        for (String name : ThemeManager.presets().keySet()) {
            final String preset = name;
            addDrawableChild(new RButton(bx, by, bw, bh, name, b -> theme.applyPreset(preset)));
            bx += bw + gap;
            if (bx + bw > px + pw - PADDING) { bx = x; by += bh + gap; }
        }

        int controlsY = by + bh + 24;

        addDrawableChild(new RSlider(x, controlsY, 220, "Accent Hue", 220f, 0f, 360f,
                v -> theme.setAccent(ColorUtil.hsvToArgb(v, 0.65f, 0.95f))));

        addDrawableChild(new RSlider(x, controlsY + 34, 220, "Panel Opacity",
                theme.getBgAlpha(), 0f, 255f, v -> theme.setBgAlpha(Math.round(v))));

        addDrawableChild(new RToggle(x, controlsY + 66, theme.isTextShadow(),
                v -> theme.setTextShadow(v)));
        shadowLabelX = x + 44;
        shadowLabelY = controlsY + 66 + 5;

        addDrawableChild(new RButton(x, controlsY + 96, 110, 22, "Reset Default",
                b -> { theme.resetDefaults(); rebuild(); }));

        addDrawableChild(new RButton(px + pw - 90, py + getPanelH() - 30, 80, 22, "Back", b -> {
            theme.save(ReevesClient.getInstance().getConfigManager());
            client.setScreen(parent);
        }));
    }

    private void rebuild() { clearChildren(); init(); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA000000);

        int px = getPanelX(), py = getPanelY(), pw = getPanelW(), ph = getPanelH();
        RenderUtil.fillRoundedRect(ctx, px, py, pw, ph, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, pw, ph, 1, ColorUtil.RC_BORDER);

        ctx.fill(px, py, px + pw, py + 36, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "Appearance", px + PADDING, py + 12, ColorUtil.RC_ACCENT);
        RenderUtil.drawText(ctx, "Presets", px + PADDING, py + 46, ColorUtil.RC_TEXT_MUTED);

        // Text-shadow label sits next to its toggle.
        RenderUtil.drawText(ctx, "Text Shadow", shadowLabelX, shadowLabelY, ColorUtil.RC_TEXT);

        // Live preview card on the right.
        renderPreview(ctx, px + pw - 230, py + 60, 210, 150);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPreview(DrawContext ctx, int x, int y, int w, int h) {
        RenderUtil.fillRoundedRect(ctx, x, y, w, h, 8, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawBorder(ctx, x, y, w, h, 1, ColorUtil.RC_BORDER);
        RenderUtil.drawText(ctx, "Preview", x + 10, y + 8, ColorUtil.RC_TEXT_MUTED);

        RenderUtil.drawText(ctx, "Primary text", x + 10, y + 28, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, "Muted text", x + 10, y + 42, ColorUtil.RC_TEXT_MUTED);

        RenderUtil.fillRoundedRect(ctx, x + 10, y + 60, w - 20, 14, 3, ColorUtil.RC_SURFACE);
        RenderUtil.drawText(ctx, "Surface", x + 16, y + 63, ColorUtil.RC_TEXT);

        RenderUtil.fillRoundedRect(ctx, x + 10, y + 82, 90, 22, 5, ColorUtil.RC_ACCENT);
        RenderUtil.drawCenteredText(ctx, "Accent", x + 55, y + 88, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    private int getPanelX() { return Math.max(10, (width - 720) / 2); }
    private int getPanelY() { return Math.max(10, (height - 460) / 2); }
    private int getPanelW() { return Math.min(width - 20, 720); }
    private int getPanelH() { return Math.min(height - 20, 460); }
}
