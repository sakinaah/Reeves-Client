package com.reevesclient.modules.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.module.ModuleManager;
import com.reevesclient.core.util.HypixelUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.modules.skyblock.SkyblockStatsModule;
import net.minecraft.client.gui.DrawContext;

/**
 * RPG-style Health / Mana / Defense bars for SkyBlock, fed by
 * {@link SkyblockStatsModule}. Draggable like any HUD element; renders nothing
 * unless the module is on, we're in SkyBlock, and stat data is available.
 */
public class RpgBarsHUD extends HUDElement {

    private static final int BAR_W = 110, BAR_H = 9, GAP = 3;

    public RpgBarsHUD() {
        super("rpg_bars", "RPG Stat Bars", 0.40f, 0.86f);
    }

    private SkyblockStatsModule module() {
        ReevesClient rc = ReevesClient.getInstance();
        if (rc == null) return null;
        ModuleManager mm = rc.getModuleManager();
        return mm == null ? null : mm.get(SkyblockStatsModule.class);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        SkyblockStatsModule m = module();
        if (m == null || !m.isEnabled() || !HypixelUtil.isInSkyBlock() || !m.hasRecentData()) return;

        int y = 0;
        drawBar(ctx, 0, y, m.getCurHealth(), m.getMaxHealth(), 0xFFE53935, "❤", m.getCurHealth() + "/" + m.getMaxHealth());
        y += BAR_H + GAP;
        drawBar(ctx, 0, y, m.getCurMana(), m.getMaxMana(), 0xFF29B6F6, "✎", m.getCurMana() + "/" + m.getMaxMana());
        y += BAR_H + GAP;
        // Defense has no max; show a fixed-fill bar with the value.
        drawBar(ctx, 0, y, 1, 1, 0xFF66BB6A, "❈", String.valueOf(m.getDefense()));
    }

    private void drawBar(DrawContext ctx, int x, int y, int cur, int max, int color, String icon, String text) {
        RenderUtil.fillRoundedRect(ctx, x, y, BAR_W, BAR_H, 2, themedBackground(0xCC202020));
        float pct = max > 0 ? Math.max(0f, Math.min(1f, (float) cur / max)) : 1f;
        int fillW = (int) (pct * (BAR_W - 2));
        if (fillW > 0) RenderUtil.fillRoundedRect(ctx, x + 1, y + 1, fillW, BAR_H - 2, 2, applyOpacity(color));
        RenderUtil.drawText(ctx, icon + " " + text, x + 4, y + 1, themedText(0xFFFFFFFF));
    }

    @Override public int getWidth()  { return BAR_W; }
    @Override public int getHeight() { return BAR_H * 3 + GAP * 2; }
}
