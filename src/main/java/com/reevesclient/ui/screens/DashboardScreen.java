package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.RButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Reeves Client main dashboard — opened with the menu key (default: Right Shift).
 * Shows: account overview, active modules, SkyBlock summary, quick-access buttons.
 */
public class DashboardScreen extends Screen {

    private final Screen parent;
    private float animAlpha = 0f;

    // Layout constants
    private static final int SIDEBAR_W   = 160;
    private static final int PADDING     = 12;
    private static final int CARD_H      = 70;

    private int selectedCategory = 0;
    private final ModuleCategory[] categories = ModuleCategory.values();

    public DashboardScreen(Screen parent) {
        super(Text.literal("Reeves Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        animAlpha = 0f;

        int btnY = height - 36;

        addDrawableChild(new RButton(width - 130, btnY, 120, 22, "HUD Editor", b ->
            client.setScreen(new HUDEditorScreen(this))));

        addDrawableChild(new RButton(width - 250, btnY, 110, 22, "Items", b ->
            client.setScreen(new ItemBrowserScreen(this))));

        addDrawableChild(new RButton(PADDING, btnY, 120, 22, "Settings", b ->
            client.setScreen(new SettingsScreen(this))));

        addDrawableChild(new RButton(PADDING + 130, btnY, 80, 22, "Close", b ->
            client.setScreen(parent)));

        addDrawableChild(new RButton(width - 360, btnY, 100, 22, "Capes", b ->
            client.setScreen(new CapeEditorScreen(this))));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        animAlpha = Math.min(1f, animAlpha + delta * 0.1f);
        int alpha = (int) (animAlpha * 220);

        // Full-screen semi-transparent backdrop
        ctx.fill(0, 0, width, height, ColorUtil.withAlpha(0x000000, alpha / 2));

        // Main window
        int panelW = Math.min(width - 40, 900);
        int panelH = Math.min(height - 40, 540);
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;

        RenderUtil.fillRoundedRect(ctx, px, py, panelW, panelH, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, panelW, panelH, 1, ColorUtil.RC_BORDER);

        // Title bar
        ctx.fill(px, py, px + panelW, py + 40, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "✦ Reeves Client", px + PADDING, py + 13, ColorUtil.RC_ACCENT);
        String version = "v" + ReevesClient.VERSION;
        RenderUtil.drawText(ctx, version, px + panelW - RenderUtil.textWidth(version) - PADDING, py + 13,
                ColorUtil.RC_TEXT_MUTED);

        // Sidebar — category list
        renderSidebar(ctx, px, py + 40, panelH - 40);

        // Main content area
        renderContent(ctx, px + SIDEBAR_W, py + 40, panelW - SIDEBAR_W, panelH - 40);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + SIDEBAR_W, y + h, ColorUtil.RC_BG_PANEL);

        // Player info
        String name = client.player != null ? client.player.getName().getString() : "—";
        RenderUtil.drawText(ctx, name, x + PADDING, y + 10, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, "Reeves Client", x + PADDING, y + 22, ColorUtil.RC_TEXT_MUTED);

        int catY = y + 50;
        for (int i = 0; i < categories.length; i++) {
            ModuleCategory cat = categories[i];
            boolean selected = i == selectedCategory;
            int bgColor = selected ? ColorUtil.RC_SURFACE : 0;
            if (bgColor != 0) ctx.fill(x, catY, x + SIDEBAR_W, catY + 22, bgColor);
            if (selected) ctx.fill(x, catY, x + 2, catY + 22, ColorUtil.RC_ACCENT);
            int textColor = selected ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED;
            RenderUtil.drawText(ctx, cat.displayName, x + PADDING + 4, catY + 7, textColor);
            catY += 24;
        }
    }

    private void renderContent(DrawContext ctx, int x, int y, int w, int h) {
        ModuleCategory cat = categories[selectedCategory];
        List<Module> modules = ReevesClient.getInstance().getModuleManager().getByCategory(cat);

        int cardX = x + PADDING;
        int cardY = y + PADDING;
        int cols   = Math.max(1, (w - PADDING * 2) / 200);
        int cardW  = (w - PADDING * (cols + 1)) / cols;

        int col = 0;
        for (Module m : modules) {
            int mx = cardX + col * (cardW + PADDING);
            renderModuleCard(ctx, mx, cardY, cardW, m);
            col++;
            if (col >= cols) { col = 0; cardY += CARD_H + PADDING; }
        }
    }

    private void renderModuleCard(DrawContext ctx, int x, int y, int w, Module m) {
        int bg = m.isEnabled() ? ColorUtil.RC_SURFACE : ColorUtil.RC_BG_PANEL;
        RenderUtil.fillRoundedRect(ctx, x, y, w, CARD_H, 6, bg);
        if (m.isEnabled()) {
            RenderUtil.drawBorder(ctx, x, y, w, CARD_H, 1, ColorUtil.RC_ACCENT);
        } else {
            RenderUtil.drawBorder(ctx, x, y, w, CARD_H, 1, ColorUtil.RC_BORDER);
        }

        int statusColor = m.isEnabled() ? ColorUtil.RC_SUCCESS : ColorUtil.RC_TEXT_MUTED;
        String status   = m.isEnabled() ? "ON" : "OFF";
        RenderUtil.drawText(ctx, m.getName(), x + 8, y + 8, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, status, x + w - RenderUtil.textWidth(status) - 8, y + 8, statusColor);

        // Description (truncated)
        String desc = m.getDescription();
        if (desc.length() > 38) desc = desc.substring(0, 35) + "…";
        RenderUtil.drawText(ctx, desc, x + 8, y + 22, ColorUtil.RC_TEXT_MUTED);

        // Visual ON/OFF switch (the whole card is clickable to toggle).
        renderToggle(ctx, x + w - 8 - 36, y + CARD_H - 8 - 18, m.isEnabled());
    }

    private void renderToggle(DrawContext ctx, int x, int y, boolean enabled) {
        int track  = enabled ? ColorUtil.RC_ACCENT : 0xFF404055;
        int thumbX = enabled ? x + 36 - 14 - 2 : x + 2;
        ctx.fill(x, y + 4, x + 36, y + 14, track);
        ctx.fill(x + 4, y, x + 32, y + 18, track);
        ctx.fill(thumbX, y + 2, thumbX + 14, y + 16, 0xFFFFFFFF);
    }

    /** Persists module state so toggles survive a restart. */
    private void saveConfig() {
        var cfg = ReevesClient.getInstance().getConfigManager();
        if (cfg != null) cfg.save();
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        double mouseX = click.x(); double mouseY = click.y();
        int panelW = Math.min(width - 40, 900);
        int panelH = Math.min(height - 40, 540);
        int px = (width - panelW) / 2;
        int py = (height - panelH) / 2;
        int sideX = px;
        int sideY = py + 40 + 50;

        for (int i = 0; i < categories.length; i++) {
            int catTop = sideY + i * 24;
            if (mouseX >= sideX && mouseX <= sideX + SIDEBAR_W
             && mouseY >= catTop && mouseY <= catTop + 22) {
                selectedCategory = i;
                return true;
            }
        }

        // Module cards — a click anywhere on a card toggles that module.
        // Grid math mirrors renderContent() exactly.
        int contentX = px + SIDEBAR_W;
        int contentY = py + 40;
        int contentW = panelW - SIDEBAR_W;
        int cardX0 = contentX + PADDING;
        int cardY  = contentY + PADDING;
        int cols   = Math.max(1, (contentW - PADDING * 2) / 200);
        int cardW  = (contentW - PADDING * (cols + 1)) / cols;

        List<Module> modules = ReevesClient.getInstance().getModuleManager()
                .getByCategory(categories[selectedCategory]);
        int col = 0;
        for (Module m : modules) {
            int mxCard = cardX0 + col * (cardW + PADDING);
            if (mouseX >= mxCard && mouseX <= mxCard + cardW
             && mouseY >= cardY && mouseY <= cardY + CARD_H) {
                m.toggle();
                saveConfig();
                return true;
            }
            col++;
            if (col >= cols) { col = 0; cardY += CARD_H + PADDING; }
        }

        return super.mouseClicked(click, shifted);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
