package com.reevesclient.api;

import com.google.gson.*;
import com.reevesclient.ReevesClient;
import com.reevesclient.modules.skyblock.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * High-level SkyBlock data access layer built on top of HypixelAPIClient.
 * Parses raw API JSON into typed model objects used by the UI and modules.
 */
public class SkyBlockAPIClient {

    private final HypixelAPIClient api;

    public SkyBlockAPIClient() {
        this.api = ReevesClient.getInstance().getHypixelAPI();
    }

    // ── Collections ──────────────────────────────────────────────────────────

    public List<CollectionTrackerModule.CollectionEntry> fetchCollections(String playerUUID) {
        List<CollectionTrackerModule.CollectionEntry> result = new ArrayList<>();
        try {
            Optional<JsonObject> profilesOpt = api.fetchSkyBlockProfiles(playerUUID)
                    .get(10, TimeUnit.SECONDS);
            if (profilesOpt.isEmpty()) return result;
            JsonObject root = profilesOpt.get();
            if (!root.has("profiles") || !root.get("success").getAsBoolean()) return result;

            JsonArray profiles = root.getAsJsonArray("profiles");
            // Find the selected (most recently played) profile
            JsonObject selected = null;
            for (JsonElement el : profiles) {
                JsonObject profile = el.getAsJsonObject();
                if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
                    selected = profile;
                    break;
                }
            }
            if (selected == null && profiles.size() > 0) selected = profiles.get(0).getAsJsonObject();
            if (selected == null) return result;

            JsonObject members = selected.getAsJsonObject("members");
            if (!members.has(playerUUID)) return result;
            JsonObject member = members.getAsJsonObject(playerUUID);
            if (!member.has("collection")) return result;

            JsonObject collections = member.getAsJsonObject("collection");
            for (Map.Entry<String, JsonElement> entry : collections.entrySet()) {
                String id    = entry.getKey();
                long   count = entry.getValue().getAsLong();
                String name  = com.reevesclient.core.util.TextUtil.skyblockIdToName(id);
                result.add(new CollectionTrackerModule.CollectionEntry(id, name, count, calculateTier(count)));
            }
            result.sort(Comparator.comparing(CollectionTrackerModule.CollectionEntry::name));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            ReevesClient.LOGGER.warn("Failed to fetch collections: {}", e.getMessage());
        }
        return result;
    }

    private int calculateTier(long count) {
        if (count >= 1_000_000) return 12;
        if (count >= 500_000)   return 11;
        if (count >= 250_000)   return 10;
        if (count >= 100_000)   return 9;
        if (count >= 50_000)    return 8;
        if (count >= 25_000)    return 7;
        if (count >= 5_000)     return 6;
        if (count >= 1_000)     return 5;
        if (count >= 250)       return 4;
        if (count >= 50)        return 3;
        if (count >= 15)        return 2;
        if (count >= 5)         return 1;
        return 0;
    }

    // ── Bazaar ───────────────────────────────────────────────────────────────

    public List<BazaarModule.BazaarProduct> fetchBazaar() {
        List<BazaarModule.BazaarProduct> result = new ArrayList<>();
        try {
            Optional<JsonObject> opt = api.fetchBazaar().get(10, TimeUnit.SECONDS);
            if (opt.isEmpty()) return result;
            JsonObject root = opt.get();
            if (!root.has("products")) return result;
            JsonObject products = root.getAsJsonObject("products");
            for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                String id = entry.getKey();
                JsonObject prod = entry.getValue().getAsJsonObject();
                JsonObject quick = prod.getAsJsonObject("quick_status");
                double buyPrice      = quick.get("buyPrice").getAsDouble();
                double sellPrice     = quick.get("sellPrice").getAsDouble();
                long   buyVolume     = quick.get("buyVolume").getAsLong();
                long   sellVolume    = quick.get("sellVolume").getAsLong();
                double buyMW         = quick.get("buyMovingWeek").getAsDouble();
                double sellMW        = quick.get("sellMovingWeek").getAsDouble();
                String name = com.reevesclient.core.util.TextUtil.skyblockIdToName(id);
                result.add(new BazaarModule.BazaarProduct(
                    id, name, buyPrice, sellPrice, buyVolume, sellVolume, buyMW, sellMW));
            }
            result.sort(Comparator.comparing(BazaarModule.BazaarProduct::name));
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to fetch bazaar: {}", e.getMessage());
        }
        return result;
    }

    // ── Auction House ────────────────────────────────────────────────────────

    public record AuctionSearchResult(List<AuctionHouseModule.AuctionEntry> entries, int totalPages) {}

    public AuctionSearchResult searchAuctions(String query, int page) {
        List<AuctionHouseModule.AuctionEntry> entries = new ArrayList<>();
        int totalPages = 1;
        try {
            Optional<JsonObject> opt = api.fetchAuctions(page).get(10, TimeUnit.SECONDS);
            if (opt.isEmpty()) return new AuctionSearchResult(entries, totalPages);
            JsonObject root = opt.get();
            totalPages = root.has("totalPages") ? root.get("totalPages").getAsInt() : 1;
            if (!root.has("auctions")) return new AuctionSearchResult(entries, totalPages);

            String lq = query.toLowerCase(Locale.ROOT);
            JsonArray auctions = root.getAsJsonArray("auctions");
            for (JsonElement el : auctions) {
                JsonObject a = el.getAsJsonObject();
                String name = a.has("item_name") ? a.get("item_name").getAsString() : "Unknown";
                if (!lq.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(lq)) continue;

                long startingBid  = a.get("starting_bid").getAsLong();
                long highestBid   = a.has("highest_bid_amount") ? a.get("highest_bid_amount").getAsLong() : 0;
                boolean isBin     = a.has("bin") && a.get("bin").getAsBoolean();
                long endTime      = a.get("end").getAsLong();
                String seller     = a.has("auctioneer") ? a.get("auctioneer").getAsString() : "";
                String rarity     = a.has("tier") ? a.get("tier").getAsString() : "";

                entries.add(new AuctionHouseModule.AuctionEntry(
                    name, startingBid, highestBid, isBin, endTime, seller, rarity));
            }
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to fetch auctions: {}", e.getMessage());
        }
        return new AuctionSearchResult(entries, totalPages);
    }
}
