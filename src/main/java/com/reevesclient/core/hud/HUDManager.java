package com.reevesclient.core.hud;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.hud.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

import java.util.*;

/** Owns all HUD elements, ticks them, and renders them in the correct order. */
public class HUDManager {

    private final List<HUDElement> elements = new ArrayList<>();
    private final Map<String, HUDElement> byId = new LinkedHashMap<>();

    public void init() {
        register(new FPSCounterHUD());
        register(new FPSGraphHUD());
        register(new CPSCounterHUD());
        register(new PingDisplayHUD());
        register(new CoordinatesHUD());
        register(new DirectionHUD());
        register(new ArmorStatusHUD());
        register(new PotionEffectsHUD());
        register(new KeystrokesHUD());
        register(new ClockHUD());
        register(new SessionTimerHUD());
        register(new WeatherHUD());
        register(new DungeonHUD());
        register(new DungeonMapHUD());

        ReevesClient.getInstance().getConfigManager().loadHUD(this);
        ReevesClient.LOGGER.info("HUDManager: {} elements loaded.", elements.size());
    }

    public void onTick(MinecraftClient client) {
        for (HUDElement el : elements) {
            // Performance: hidden elements don't need per-tick data updates.
            if (!el.isVisible()) continue;
            try { el.onTick(client); }
            catch (Exception e) {
                ReevesClient.LOGGER.error("Error ticking HUD element '{}': {}", el.getId(), e.getMessage(), e);
            }
        }
    }

    public void render(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return;

        for (HUDElement el : elements) {
            if (!el.isVisible()) continue;
            try {
                renderElement(context, el, tickDelta);
            } catch (Exception e) {
                ReevesClient.LOGGER.error("Error rendering HUD element '{}': {}", el.getId(), e.getMessage(), e);
            }
        }
    }

    /** Renders a single element with its own matrix (position + scale). */
    public static void renderElement(DrawContext context, HUDElement el, float tickDelta) {
        int x = el.getPosition().getPixelX();
        int y = el.getPosition().getPixelY();
        float scale = el.getPosition().getScale();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) x, (float) y);
        context.getMatrices().scale(scale, scale);

        el.render(context, tickDelta);

        context.getMatrices().popMatrix();
    }

    private void register(HUDElement el) {
        elements.add(el);
        byId.put(el.getId(), el);
    }

    public List<HUDElement>        getElements()        { return Collections.unmodifiableList(elements); }
    public HUDElement              getById(String id)   { return byId.get(id); }
}
