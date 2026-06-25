package com.reevesclient.modules.skyblock;

import com.google.gson.JsonObject;
import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;
import com.reevesclient.core.module.settings.ModuleSetting.IntSetting;
import com.reevesclient.core.util.HypixelUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds SkyBlock Bazaar prices to item tooltips. Prices come from the public
 * Hypixel Bazaar endpoint (no API key required), cached and refreshed off-thread
 * so the render loop never blocks.
 *
 * SAFETY: read-only market information. It never buys, sells, or interacts with
 * the economy — it only annotates tooltips.
 */
public class EconomyTooltipModule extends Module {

    private final BooleanSetting showBazaar;
    private final IntSetting     refreshMinutes;

    // SkyBlock id (UPPERCASE) -> { instaBuyPrice, instaSellPrice }
    private final Map<String, double[]> bazaar = new ConcurrentHashMap<>();
    private volatile long    lastFetchMs = 0;
    private volatile boolean fetching    = false;

    public EconomyTooltipModule() {
        super("economy_tooltips", "Economy Tooltips",
                "Shows Bazaar buy/sell prices in item tooltips. Informational only.",
                ModuleCategory.SKYBLOCK, true);
        showBazaar     = addSetting(new BooleanSetting("show_bazaar", "Bazaar Prices",
                "Show Bazaar buy/sell prices in tooltips.", true));
        refreshMinutes = addSetting(new IntSetting("refresh_minutes", "Refresh (min)",
                "How often to refresh Bazaar prices.", 5, 1, 60));
    }

    @Override
    public void init() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!isEnabled() || !showBazaar.getValue() || bazaar.isEmpty()) return;
            String id = HypixelUtil.getSkyblockId(stack);
            if (id.isEmpty()) return;
            double[] price = bazaar.get(id.toUpperCase());
            if (price == null) return;
            lines.add(Text.literal("Bazaar buy: " + formatCoins(price[0])).formatted(Formatting.GOLD));
            lines.add(Text.literal("Bazaar sell: " + formatCoins(price[1])).formatted(Formatting.YELLOW));
        });
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !showBazaar.getValue()) return;
        if (!HypixelUtil.isInSkyBlock()) return;
        long now = System.currentTimeMillis();
        if (fetching || now - lastFetchMs < refreshMinutes.getValue() * 60_000L) return;
        refresh();
    }

    private void refresh() {
        fetching = true;
        ReevesClient.getInstance().getHypixelAPI().fetchBazaarPublic().thenAccept(opt -> {
            try {
                opt.ifPresent(this::parseBazaar);
            } finally {
                lastFetchMs = System.currentTimeMillis();
                fetching = false;
            }
        });
    }

    private void parseBazaar(JsonObject root) {
        if (!root.has("products") || !root.get("products").isJsonObject()) return;
        JsonObject products = root.getAsJsonObject("products");
        Map<String, double[]> next = new java.util.HashMap<>();
        for (Map.Entry<String, com.google.gson.JsonElement> e : products.entrySet()) {
            if (!e.getValue().isJsonObject()) continue;
            JsonObject prod = e.getValue().getAsJsonObject();
            if (!prod.has("quick_status") || !prod.get("quick_status").isJsonObject()) continue;
            JsonObject qs = prod.getAsJsonObject("quick_status");
            double buy  = qs.has("buyPrice")  ? qs.get("buyPrice").getAsDouble()  : 0;
            double sell = qs.has("sellPrice") ? qs.get("sellPrice").getAsDouble() : 0;
            next.put(e.getKey().toUpperCase(), new double[]{buy, sell});
        }
        bazaar.clear();
        bazaar.putAll(next);
    }

    /** Formats a coin amount like 1.2M / 340.5k / 980. */
    public static String formatCoins(double v) {
        if (v >= 1_000_000_000) return String.format("%.2fB", v / 1_000_000_000);
        if (v >= 1_000_000)     return String.format("%.2fM", v / 1_000_000);
        if (v >= 1_000)         return String.format("%.1fk", v / 1_000);
        return String.format("%.0f", v);
    }
}
