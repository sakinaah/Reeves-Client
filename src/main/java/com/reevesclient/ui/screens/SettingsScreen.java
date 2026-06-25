package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

/**
 * Full settings screen.
 * Left sidebar: category tabs.
 * Center: scrollable module list with toggle + settings expansion.
 * Right: per-module settings panel.
 */
public class SettingsScreen extends Screen {

    private final Screen parent;

    private ModuleCategory selectedCategory = ModuleCategory.HUD;
    private Module         selectedModule   = null;
    private String         searchText       = "";
    private int            scrollOffset     = 0;

    private RTextField searchField;

    private static final int SIDEBAR_W    = 120;
    private static final int MODULE_LIST_W = 200;
    private static final int PADDING      = 8;
    private static final int ITEM_H       = 28;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int px = getPanelX();
        int py = getPanelY();

        searchField = new RTextField(px + SIDEBAR_W + PADDING, py + 44, MODULE_LIST_W - PADDING * 2, 18, "Search…");
        searchField.setChangedListener(s -> { searchText = s; scrollOffset = 0; selectedModule = null; });
        addDrawableChild(searchField);

        addDrawableChild(new RButton(px + getPanelW() - 90, py + getPanelH() - 30, 80, 22, "Back",
                b -> client.setScreen(parent)));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Background
        ctx.fill(0, 0, width, height, 0xAA000000);

        int px = getPanelX(), py = getPanelY(), pw = getPanelW(), ph = getPanelH();
        RenderUtil.fillRoundedRect(ctx, px, py, pw, ph, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, pw, ph, 1, ColorUtil.RC_BORDER);

        // Title
        ctx.fill(px, py, px + pw, py + 36, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "Settings", px + PADDING, py + 12, ColorUtil.RC_TEXT);

        // Category sidebar
        renderCategorySidebar(ctx, px, py + 36, ph - 36);

        // Module list
        renderModuleList(ctx, px + SIDEBAR_W, py + 36, ph - 36);

        // Module settings panel
        if (selectedModule != null) {
            renderModuleSettings(ctx, px + SIDEBAR_W + MODULE_LIST_W, py + 36,
                    pw - SIDEBAR_W - MODULE_LIST_W, ph - 36);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderCategorySidebar(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + SIDEBAR_W, y + h, ColorUtil.RC_BG_PANEL);
        int catY = y + PADDING;
        for (ModuleCategory cat : ModuleCategory.values()) {
            boolean sel = cat == selectedCategory;
            if (sel) {
                ctx.fill(x, catY, x + SIDEBAR_W, catY + 24, ColorUtil.RC_SURFACE);
                ctx.fill(x, catY, x + 2, catY + 24, cat.accentColor);
            }
            RenderUtil.drawText(ctx, cat.displayName, x + PADDING, catY + 8,
                    sel ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED);
            catY += 26;
        }
    }

    private void renderModuleList(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + MODULE_LIST_W, y + h, 0xFF141426);
        // search field sits at y+8
        int listY = y + 36 + PADDING;
        int maxH  = h - 50;

        List<Module> modules = getFilteredModules();
        int idx = 0;
        for (Module m : modules) {
            int itemY = listY + idx * ITEM_H - scrollOffset;
            if (itemY < y + 36 || itemY + ITEM_H > y + h - 28) { idx++; continue; }

            boolean sel = m == selectedModule;
            if (sel) ctx.fill(x, itemY, x + MODULE_LIST_W, itemY + ITEM_H, ColorUtil.RC_SURFACE);

            int statusColor = m.isEnabled() ? ColorUtil.RC_SUCCESS : ColorUtil.RC_ERROR;
            ctx.fill(x + PADDING, itemY + 8, x + PADDING + 6, itemY + 20, statusColor);
            RenderUtil.drawText(ctx, m.getName(), x + PADDING + 10, itemY + 10,
                    sel ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED);
            idx++;
        }
    }

    private void renderModuleSettings(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, ColorUtil.RC_BG_PANEL);
        if (selectedModule == null) return;

        Module m = selectedModule;
        int dy = y + PADDING;

        // Module name and toggle header
        RenderUtil.drawText(ctx, m.getName(), x + PADDING, dy, ColorUtil.RC_TEXT);
        dy += 14;
        String desc = m.getDescription();
        RenderUtil.drawText(ctx, desc, x + PADDING, dy, ColorUtil.RC_TEXT_MUTED);
        dy += 16;

        ctx.fill(x + PADDING, dy, x + w - PADDING, dy + 1, ColorUtil.RC_BORDER);
        dy += 8;

        // Settings list
        for (ModuleSetting<?> setting : m.getSettings()) {
            RenderUtil.drawText(ctx, setting.getDisplayName(), x + PADDING, dy, ColorUtil.RC_TEXT);
            dy += 12;
            RenderUtil.drawText(ctx, setting.getDescription(), x + PADDING, dy, ColorUtil.RC_TEXT_MUTED);
            dy += 10;
            RenderUtil.drawText(ctx, "Value: " + setting.getValue(), x + PADDING, dy, ColorUtil.RC_ACCENT);
            dy += 16;
        }
    }

    private List<Module> getFilteredModules() {
        if (!searchText.isEmpty()) {
            return ReevesClient.getInstance().getModuleManager().search(searchText);
        }
        return ReevesClient.getInstance().getModuleManager().getByCategory(selectedCategory);
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        double mx = click.x(); double my = click.y(); int button = click.button();
        int px = getPanelX(), py = getPanelY();
        int catSideX = px;
        int catSideY = py + 36 + PADDING;

        int catIdx = 0;
        for (ModuleCategory cat : ModuleCategory.values()) {
            int cy = catSideY + catIdx * 26;
            if (mx >= catSideX && mx <= catSideX + SIDEBAR_W && my >= cy && my <= cy + 26) {
                selectedCategory = cat;
                scrollOffset = 0;
                selectedModule = null;
                return true;
            }
            catIdx++;
        }

        int listX = px + SIDEBAR_W;
        int listY = py + 36 + 36 + PADDING;
        List<Module> modules = getFilteredModules();
        for (int i = 0; i < modules.size(); i++) {
            int itemY = listY + i * ITEM_H - scrollOffset;
            if (mx >= listX && mx <= listX + MODULE_LIST_W && my >= itemY && my <= itemY + ITEM_H) {
                if (button == 0) selectedModule = modules.get(i);
                if (button == 1 && selectedModule != null) selectedModule.toggle();
                return true;
            }
        }

        return super.mouseClicked(click, shifted);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        scrollOffset = Math.max(0, (int)(scrollOffset - vScroll * 12));
        return true;
    }

    private int getPanelX() { return Math.max(10, (width - 860) / 2); }
    private int getPanelY() { return Math.max(10, (height - 540) / 2); }
    private int getPanelW() { return Math.min(width - 20, 860); }
    private int getPanelH() { return Math.min(height - 20, 540); }

    @Override
    public boolean shouldPause() { return false; }
}
