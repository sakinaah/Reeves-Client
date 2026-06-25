package com.reevesclient.modules.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.module.ModuleManager;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.modules.dungeons.DungeonScoreModule;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Draggable HUD overlay for the dungeon tracker. Reads parsed values from
 * {@link DungeonScoreModule}; renders nothing unless that module is enabled and
 * the player is in a dungeon. Visual/informational only.
 */
public class DungeonHUD extends HUDElement {

    public DungeonHUD() {
        super("dungeon_hud", "Dungeon Tracker", 0.01f, 0.30f);
    }

    private DungeonScoreModule module() {
        ReevesClient rc = ReevesClient.getInstance();
        if (rc == null) return null;
        ModuleManager mm = rc.getModuleManager();
        return mm == null ? null : mm.get(DungeonScoreModule.class);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        DungeonScoreModule m = module();
        if (m == null || !m.isEnabled() || !HypixelUtil.isInDungeon()) return;

        List<String> lines = new ArrayList<>();
        String floor = m.getFloor().isEmpty() ? "Dungeon" : "Dungeon (" + m.getFloor() + ")";
        lines.add(floor);
        lines.add("⏱ " + m.getElapsedString() + "   " + m.getClearedPct() + "%");
        if (m.showSecrets()) lines.add("Secrets: " + m.getFoundSecrets());
        if (m.showCrypts())  lines.add("Crypts: " + m.getCrypts());
        if (m.showDeaths())  lines.add("Deaths: " + m.getDeaths());
        if (m.getTotalPuzzles() > 0) lines.add("Puzzles: " + m.getCompletedPuzzles() + "/" + m.getTotalPuzzles());
        if (m.showScore())   lines.add("Score: ~" + m.getEstimatedScore() + " (" + m.getEstimatedGrade() + ")");

        int pad = 4;
        int lineH = 10;
        int boxW = getWidth();
        int boxH = pad * 2 + lines.size() * lineH;

        if (isShowBackground()) {
            RenderUtil.fillRoundedRect(ctx, 0, 0, boxW, boxH, 4, themedBackground(0xCC101020));
        }

        int accent = themedAccent(0xFFFF9800);
        int text   = themedText(0xFFE0E0E0);
        int y = pad;
        for (int i = 0; i < lines.size(); i++) {
            RenderUtil.drawText(ctx, lines.get(i), pad, y, i == 0 ? accent : text);
            y += lineH;
        }
    }

    @Override public int getWidth()  { return 120; }
    @Override public int getHeight() { return 78; }
}
