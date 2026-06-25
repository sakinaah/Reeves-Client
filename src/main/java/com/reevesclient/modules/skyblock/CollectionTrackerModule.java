package com.reevesclient.modules.skyblock;

import com.reevesclient.api.SkyBlockAPIClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.ReevesClient;

import java.util.*;

/**
 * Displays SkyBlock collection data fetched from the Hypixel public API.
 * Read-only informational display. No game interaction.
 */
public class CollectionTrackerModule extends Module {

    public record CollectionEntry(String id, String name, long count, int tier) {}

    private final List<CollectionEntry> collections  = new ArrayList<>();
    private volatile boolean loading = false;
    private String lastLoadedPlayer  = "";

    public CollectionTrackerModule() {
        super("collection_tracker", "Collection Tracker",
              "View SkyBlock item collection counts and tiers via the Hypixel API.",
              ModuleCategory.SKYBLOCK, false);
    }

    /** Triggers an async API fetch for the given player UUID. */
    public void fetchFor(String playerUUID) {
        if (loading || playerUUID.equals(lastLoadedPlayer)) return;
        loading = true;
        collections.clear();
        Thread.ofVirtual().name("reeves-collection-fetch").start(() -> {
            try {
                SkyBlockAPIClient api = ReevesClient.getInstance().getSkyBlockAPI();
                List<CollectionEntry> fetched = api.fetchCollections(playerUUID);
                synchronized (collections) {
                    collections.clear();
                    collections.addAll(fetched);
                }
                lastLoadedPlayer = playerUUID;
            } finally {
                loading = false;
            }
        });
    }

    public List<CollectionEntry> getCollections() {
        synchronized (collections) { return new ArrayList<>(collections); }
    }

    public boolean isLoading() { return loading; }
}
