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
        register(new RpgBarsHUD());

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

    // ── Layout presets (visibility sets) ──────────────────────────────────────
    // A null set means "everything visible".
    private static final Map<String, Set<String>> LAYOUTS = new LinkedHashMap<>();
    static {
        LAYOUTS.put("Default",  null);
        LAYOUTS.put("SkyBlock", Set.of("coordinates", "clock", "session_timer", "rpg_bars", "fps_counter", "ping_display"));
        LAYOUTS.put("Dungeons", Set.of("dungeon_hud", "dungeon_map", "fps_counter", "ping_display", "coordinates"));
        LAYOUTS.put("PvP",      Set.of("fps_counter", "cps_counter", "ping_display", "keystrokes", "armor_status", "potion_effects"));
        LAYOUTS.put("Minimal",  Set.of("fps_counter", "coordinates"));
    }

    public static Set<String> layoutNames() { return LAYOUTS.keySet(); }

    /** Applies a named layout by toggling element visibility, then saves. */
    public void applyLayout(String name) {
        Set<String> visible = LAYOUTS.get(name);
        for (HUDElement el : elements) {
            el.setVisible(visible == null || visible.contains(el.getId()));
        }
        var cfg = ReevesClient.getInstance().getConfigManager();
        if (cfg != null) cfg.save();
    }

    private void register(HUDElement el) {
        elements.add(el);
        byId.put(el.getId(), el);
    }

    public List<HUDElement>        getElements()        { return Collections.unmodifiableList(elements); }
    public HUDElement              getById(String id)   { return byId.get(id); }
}
