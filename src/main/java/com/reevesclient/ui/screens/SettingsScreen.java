package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

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
    private static final int CAT_TOGGLE_W = 28;
    private static final int CAT_TOGGLE_H = 14;

    // Interactive setting controls captured during render of the settings panel.
    // kind: 'B' boolean toggle, 'N' int/float slider, 'E' enum cycle, 'C' colour swatch.
    private static final class SettingControl {
        final ModuleSetting<?> setting; final int x, y, w, h; final char kind;
        SettingControl(ModuleSetting<?> s, int x, int y, int w, int h, char kind) {
            this.setting = s; this.x = x; this.y = y; this.w = w; this.h = h; this.kind = kind;
        }
        boolean hit(double mx, double my) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
    }
    private final java.util.List<SettingControl> settingControls = new java.util.ArrayList<>();
    private SettingControl draggingControl = null;

    private static final int[] COLOR_PALETTE = {
        0xFF5B8DEE, 0xFFE53935, 0xFF4CAF50, 0xFFFF9800, 0xFFB061FF, 0xFF00BCD4, 0xFFFFFFFF, 0xFF000000
    };

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

        addDrawableChild(new RButton(px + getPanelW() - 185, py + getPanelH() - 30, 90, 22, "Capes",
            b -> client.setScreen(new CapeEditorScreen(this))));

        addDrawableChild(new RButton(px + getPanelW() - 285, py + getPanelH() - 30, 95, 22, "Appearance",
            b -> client.setScreen(new AppearanceScreen(this))));
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
            RenderUtil.drawText(ctx, cat.displayName, x + PADDING, catY + 7,
                    sel ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED);
            // Category-wide master toggle (enables/disables every module in the category).
            int tX = x + SIDEBAR_W - PADDING - CAT_TOGGLE_W;
            int tY = catY + (24 - CAT_TOGGLE_H) / 2;
            renderToggle(ctx, tX, tY, CAT_TOGGLE_W, CAT_TOGGLE_H, isCategoryAllEnabled(cat));
            catY += 26;
        }
    }

    private boolean isCategoryAllEnabled(ModuleCategory cat) {
        List<Module> mods = ReevesClient.getInstance().getModuleManager().getByCategory(cat);
        if (mods.isEmpty()) return false;
        for (Module m : mods) if (!m.isEnabled()) return false;
        return true;
    }

    private void setCategoryEnabled(ModuleCategory cat, boolean enabled) {
        for (Module m : ReevesClient.getInstance().getModuleManager().getByCategory(cat)) {
            m.setEnabled(enabled);
        }
        saveConfig();
    }

    /** Persists module/HUD state so toggles survive a restart. */
    private void saveConfig() {
        var cfg = ReevesClient.getInstance().getConfigManager();
        if (cfg != null) cfg.save();
    }

    private void renderModuleList(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + MODULE_LIST_W, y + h, 0xFF141426);
        // search field sits at y+8
        int listY = y + 36 + PADDING;

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

            int toggleX = x + MODULE_LIST_W - PADDING - 36;
            int toggleY = itemY + 5;
            renderToggle(ctx, toggleX, toggleY, m.isEnabled());
            idx++;
        }
    }

    private void renderModuleSettings(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, ColorUtil.RC_BG_PANEL);
        settingControls.clear();
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

        // Settings list — every setting type gets an interactive control.
        for (ModuleSetting<?> setting : m.getSettings()) {
            int labelY = dy;
            RenderUtil.drawText(ctx, setting.getDisplayName(), x + PADDING, dy, ColorUtil.RC_TEXT);
            dy += 12;
            RenderUtil.drawText(ctx, setting.getDescription(), x + PADDING, dy, ColorUtil.RC_TEXT_MUTED);
            dy += 12;

            if (setting instanceof BooleanSetting bs) {
                int tx = x + w - PADDING - 36, ty = labelY - 1;
                renderToggle(ctx, tx, ty, bs.getValue());
                settingControls.add(new SettingControl(setting, tx, ty, 36, 18, 'B'));
            } else if (setting instanceof ModuleSetting.IntSetting is) {
                String v = String.valueOf(is.getValue());
                RenderUtil.drawText(ctx, v, x + w - PADDING - RenderUtil.textWidth(v), labelY, ColorUtil.RC_ACCENT);
                int sx = x + PADDING, sw = w - PADDING * 2;
                float pct = (is.getValue() - is.getMin()) / (float) Math.max(1, is.getMax() - is.getMin());
                renderSettingSlider(ctx, sx, dy, sw, pct);
                settingControls.add(new SettingControl(setting, sx, dy - 2, sw, 12, 'N'));
                dy += 12;
            } else if (setting instanceof ModuleSetting.FloatSetting fs) {
                String v = String.format("%.2f", fs.getValue());
                RenderUtil.drawText(ctx, v, x + w - PADDING - RenderUtil.textWidth(v), labelY, ColorUtil.RC_ACCENT);
                int sx = x + PADDING, sw = w - PADDING * 2;
                float pct = (fs.getValue() - fs.getMin()) / Math.max(0.0001f, fs.getMax() - fs.getMin());
                renderSettingSlider(ctx, sx, dy, sw, pct);
                settingControls.add(new SettingControl(setting, sx, dy - 2, sw, 12, 'N'));
                dy += 12;
            } else if (setting instanceof ModuleSetting.EnumSetting<?> es) {
                String val = String.valueOf(es.getValue());
                int pillW = RenderUtil.textWidth(val) + 12;
                RenderUtil.fillRoundedRect(ctx, x + PADDING, dy, pillW, 13, 3, ColorUtil.RC_SURFACE);
                RenderUtil.drawText(ctx, val, x + PADDING + 6, dy + 3, ColorUtil.RC_ACCENT);
                settingControls.add(new SettingControl(setting, x + PADDING, dy, pillW, 13, 'E'));
                dy += 15;
            } else if (setting instanceof ModuleSetting.ColorSetting cs) {
                RenderUtil.fillRoundedRect(ctx, x + PADDING, dy, 26, 13, 3, 0xFF000000 | (cs.getValue() & 0xFFFFFF));
                RenderUtil.drawBorder(ctx, x + PADDING, dy, 26, 13, 1, ColorUtil.RC_BORDER);
                RenderUtil.drawText(ctx, "click to change", x + PADDING + 32, dy + 3, ColorUtil.RC_TEXT_MUTED);
                settingControls.add(new SettingControl(setting, x + PADDING, dy, 26, 13, 'C'));
                dy += 15;
            } else {
                RenderUtil.drawText(ctx, "Value: " + setting.getValue(), x + PADDING, dy, ColorUtil.RC_ACCENT);
                dy += 12;
            }
            dy += 8;
        }
    }

    private void renderSettingSlider(DrawContext ctx, int x, int y, int w, float pct) {
        pct = Math.max(0f, Math.min(1f, pct));
        ctx.fill(x, y + 3, x + w, y + 5, 0xFF404055);
        ctx.fill(x, y + 3, x + (int) (pct * w), y + 5, ColorUtil.RC_ACCENT);
        int tx = x + (int) (pct * w) - 2;
        ctx.fill(tx, y, tx + 4, y + 8, 0xFFFFFFFF);
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
                // Category-wide master toggle takes priority over selecting the category.
                int tX = catSideX + SIDEBAR_W - PADDING - CAT_TOGGLE_W;
                int tY = cy + (24 - CAT_TOGGLE_H) / 2;
                if (mx >= tX && mx <= tX + CAT_TOGGLE_W && my >= tY && my <= tY + CAT_TOGGLE_H) {
                    setCategoryEnabled(cat, !isCategoryAllEnabled(cat));
                    return true;
                }
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
                Module clicked = modules.get(i);
                int toggleX = listX + MODULE_LIST_W - PADDING - 36;
                int toggleY = itemY + 5;
                boolean onToggle = mx >= toggleX && mx <= toggleX + 36 && my >= toggleY && my <= toggleY + 18;

                if (button == 0 && onToggle) {
                    clicked.toggle();
                    selectedModule = clicked;
                    saveConfig();
                    return true;
                }

                if (button == 0) selectedModule = clicked;
                if (button == 1) { clicked.toggle(); saveConfig(); }
                return true;
            }
        }

        // Interactive setting controls in the right-hand panel.
        for (SettingControl c : settingControls) {
            if (!c.hit(mx, my)) continue;
            handleControlClick(c, mx);
            if (c.kind != 'N') saveConfig(); // sliders save on release
            return true;
        }

        return super.mouseClicked(click, shifted);
    }

    private void handleControlClick(SettingControl c, double mx) {
        switch (c.kind) {
            case 'B' -> ((BooleanSetting) c.setting).toggle();
            case 'N' -> { draggingControl = c; setNumericFromX(c, mx); }
            case 'E' -> ((ModuleSetting.EnumSetting<?>) c.setting).cycle();
            case 'C' -> cycleColor((ModuleSetting.ColorSetting) c.setting);
        }
    }

    private void setNumericFromX(SettingControl c, double mx) {
        float pct = (float) Math.max(0, Math.min(1, (mx - c.x) / c.w));
        if (c.setting instanceof ModuleSetting.IntSetting is) {
            is.setValue(Math.round(is.getMin() + pct * (is.getMax() - is.getMin())));
        } else if (c.setting instanceof ModuleSetting.FloatSetting fs) {
            fs.setValue(fs.getMin() + pct * (fs.getMax() - fs.getMin()));
        }
    }

    private void cycleColor(ModuleSetting.ColorSetting cs) {
        int cur = cs.getValue() & 0xFFFFFF;
        int idx = 0;
        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            if ((COLOR_PALETTE[i] & 0xFFFFFF) == cur) { idx = i + 1; break; }
        }
        cs.setValue(COLOR_PALETTE[idx % COLOR_PALETTE.length]);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (draggingControl != null) {
            setNumericFromX(draggingControl, click.x());
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingControl != null) {
            draggingControl = null;
            saveConfig(); // persist the final slider value once
        }
        return super.mouseReleased(click);
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

    private void renderToggle(DrawContext ctx, int x, int y, boolean enabled) {
        renderToggle(ctx, x, y, 36, 18, enabled);
    }

    /** Draws a switch-style toggle of arbitrary size. */
    private void renderToggle(DrawContext ctx, int x, int y, int w, int h, boolean enabled) {
        int track  = enabled ? ColorUtil.RC_ACCENT : 0xFF404055;
        int inset  = Math.max(2, h / 4);
        int thumb  = h - 4;
        int thumbX = enabled ? x + w - thumb - 2 : x + 2;
        ctx.fill(x, y + inset, x + w, y + h - inset, track);
        ctx.fill(x + inset, y, x + w - inset, y + h, track);
        ctx.fill(thumbX, y + 2, thumbX + thumb, y + h - 2, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() { return false; }
}
