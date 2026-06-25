package com.reevesclient.modules.skyblock;

import com.reevesclient.ReevesClient;
import com.reevesclient.api.SkyBlockAPIClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

import java.util.*;

/**
 * Fetches and displays Auction House listings from the Hypixel public API.
 * Read-only informational browser. All bidding must be done by the player in-game.
 */
public class AuctionHouseModule extends Module {

    public record AuctionEntry(
        String  itemName,
        long    startingBid,
        long    highestBid,
        boolean isBin,
        long    endTimestamp,
        String  sellerName,
        String  rarity
    ) {}

    private final List<AuctionEntry> auctions = new ArrayList<>();
    private volatile boolean loading = false;
    private String  searchQuery = "";
    private int     currentPage = 0;
    private int     totalPages  = 1;

    public AuctionHouseModule() {
        super("auction_house", "Auction House Viewer",
              "Browse Auction House data from the Hypixel API.",
              ModuleCategory.SKYBLOCK, false);
    }

    public void search(String query, int page) {
        if (loading) return;
        this.searchQuery = query;
        this.currentPage = page;
        loading = true;
        auctions.clear();

        Thread.ofVirtual().name("reeves-ah-fetch").start(() -> {
            try {
                SkyBlockAPIClient api = ReevesClient.getInstance().getSkyBlockAPI();
                var result = api.searchAuctions(query, page);
                synchronized (auctions) {
                    auctions.clear();
                    auctions.addAll(result.entries());
                    totalPages = result.totalPages();
                }
            } finally {
                loading = false;
            }
        });
    }

    public List<AuctionEntry> getAuctions() {
        synchronized (auctions) { return new ArrayList<>(auctions); }
    }

    public boolean isLoading()    { return loading; }
    public String  getQuery()     { return searchQuery; }
    public int     getCurrentPage() { return currentPage; }
    public int     getTotalPages()  { return totalPages; }
}
