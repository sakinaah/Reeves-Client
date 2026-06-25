package com.reevesclient.modules.skyblock;

import com.reevesclient.ReevesClient;
import com.reevesclient.api.SkyBlockAPIClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

import java.util.*;

/**
 * Fetches Bazaar price data from the Hypixel public API.
 * Displays buy/sell prices, weekly volume, and trends.
 * Informational only — no interaction with the game economy.
 */
public class BazaarModule extends Module {

    public record BazaarProduct(
        String id,
        String name,
        double buyPrice,
        double sellPrice,
        long   buyVolume,
        long   sellVolume,
        double buyMovingWeek,
        double sellMovingWeek
    ) {
        public double margin()        { return buyPrice - sellPrice; }
        public double marginPercent() { return sellPrice > 0 ? (margin() / sellPrice) * 100 : 0; }
    }

    private final List<BazaarProduct>     products = new ArrayList<>();
    private final Map<String,BazaarProduct> byId   = new LinkedHashMap<>();
    private volatile boolean loading = false;
    private long lastFetchMs = 0;
    private static final long CACHE_TTL = 60_000; // refresh every 60s max

    public BazaarModule() {
        super("bazaar", "Bazaar Viewer",
              "Browse Bazaar buy/sell prices and weekly volume from the Hypixel API.",
              ModuleCategory.SKYBLOCK, false);
    }

    public void fetch() {
        if (loading) return;
        long now = System.currentTimeMillis();
        if (now - lastFetchMs < CACHE_TTL) return; // use cached data
        loading = true;

        Thread.ofVirtual().name("reeves-bazaar-fetch").start(() -> {
            try {
                SkyBlockAPIClient api = ReevesClient.getInstance().getSkyBlockAPI();
                List<BazaarProduct> fetched = api.fetchBazaar();
                synchronized (products) {
                    products.clear();
                    byId.clear();
                    products.addAll(fetched);
                    fetched.forEach(p -> byId.put(p.id(), p));
                }
                lastFetchMs = System.currentTimeMillis();
            } finally {
                loading = false;
            }
        });
    }

    public List<BazaarProduct> getProducts() {
        synchronized (products) { return new ArrayList<>(products); }
    }

    public Optional<BazaarProduct> getById(String id) {
        synchronized (products) { return Optional.ofNullable(byId.get(id)); }
    }

    /** Returns top N products sorted by margin. */
    public List<BazaarProduct> getTopByMargin(int n) {
        synchronized (products) {
            return products.stream()
                .sorted(Comparator.comparingDouble(BazaarProduct::margin).reversed())
                .limit(n)
                .toList();
        }
    }

    public boolean isLoading()   { return loading; }
    public long    getLastFetch() { return lastFetchMs; }
}
