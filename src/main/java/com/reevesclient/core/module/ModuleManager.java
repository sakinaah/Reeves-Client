package com.reevesclient.core.module;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.hud.*;
import com.reevesclient.modules.performance.*;
import com.reevesclient.modules.utility.*;
import com.reevesclient.modules.skyblock.*;
import com.reevesclient.modules.dungeons.*;
import com.reevesclient.modules.pvp.*;
import net.minecraft.client.MinecraftClient;

import java.util.*;
import java.util.stream.Collectors;

/** Owns and ticks all registered modules. */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byId = new LinkedHashMap<>();

    public void init() {
        // ── HUD ────────────────────────────────────────────────────────────
        register(new FPSCounterModule());
        register(new FPSGraphModule());
        register(new CPSCounterModule());
        register(new PingDisplayModule());
        register(new CoordinatesModule());
        register(new DirectionModule());
        register(new ArmorStatusModule());
        register(new PotionEffectsModule());
        register(new KeystrokesModule());
        register(new ClockModule());
        register(new SessionTimerModule());
        register(new WeatherModule());

        // ── Performance ────────────────────────────────────────────────────
        register(new EntityRenderModule());
        register(new ParticleReducerModule());
        register(new PerformanceProfileModule());

        // ── Utility ────────────────────────────────────────────────────────
        register(new ToggleSprintModule());
        register(new ToggleSneakModule());
        register(new ChatEnhancementsModule());
        register(new WaypointModule());
        register(new ScreenshotManagerModule());

        // ── SkyBlock ───────────────────────────────────────────────────────
        register(new SkillTrackerModule());
        register(new SlayerTrackerModule());
        register(new CollectionTrackerModule());
        register(new MinionTrackerModule());
        register(new GardenDashboardModule());
        register(new SkyBlockCalendarModule());
        register(new AuctionHouseModule());
        register(new BazaarModule());
        register(new ProfitCalculatorModule());
        register(new GoalTrackerModule());

        // ── Dungeons ───────────────────────────────────────────────────────
        register(new DungeonRoomModule());
        register(new SecretWaypointModule());
        register(new DungeonPartyModule());
        register(new DungeonScoreModule());

        // ── PvP ────────────────────────────────────────────────────────────
        register(new CrosshairModule());
        register(new HitEffectModule());

        // Apply saved config to each module
        ReevesClient.getInstance().getConfigManager().loadModules(this);

        // Init each module after config is applied
        for (Module m : modules) {
            try { m.init(); }
            catch (Exception e) {
                ReevesClient.LOGGER.error("Failed to init module '{}': {}", m.getId(), e.getMessage(), e);
            }
        }

        ReevesClient.LOGGER.info("ModuleManager: {} modules loaded.", modules.size());
    }

    public void onTick(MinecraftClient client) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onTick(client); }
                catch (Exception e) {
                    ReevesClient.LOGGER.error("Error in module tick '{}': {}", m.getId(), e.getMessage(), e);
                }
            }
        }
    }

    private void register(Module module) {
        modules.add(module);
        byId.put(module.getId(), module);
    }

    public List<Module> getAll()                                { return Collections.unmodifiableList(modules); }
    public Module       getById(String id)                      { return byId.get(id); }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) modules.stream().filter(type::isInstance).findFirst().orElse(null);
    }

    public List<Module> getByCategory(ModuleCategory category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public List<Module> search(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        return modules.stream()
                .filter(m -> m.getName().toLowerCase(Locale.ROOT).contains(q)
                          || m.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .collect(Collectors.toList());
    }
}
