package com.reevesclient.api;

import com.google.gson.*;
import com.reevesclient.ReevesClient;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Thin client for the official Hypixel Public API (api.hypixel.net).
 * Requires the user to configure their own API key in settings.
 *
 * Rate limits: 120 requests/min per key — we cache aggressively.
 */
public class HypixelAPIClient {

    private static final String BASE_URL = "https://api.hypixel.net/v2";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient http;
    private String apiKey = "";

    public HypixelAPIClient() {
        http = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void setApiKey(String key) { this.apiKey = key; }
    public String getApiKey()         { return apiKey; }
    public boolean hasApiKey()        { return apiKey != null && !apiKey.isBlank(); }

    // ── Generic request ──────────────────────────────────────────────────────

    private CompletableFuture<Optional<JsonObject>> getJson(String path) {
        if (!hasApiKey()) {
            ReevesClient.LOGGER.warn("Hypixel API key not configured.");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("API-Key", apiKey)
                .header("Accept", "application/json")
                .GET()
                .timeout(TIMEOUT)
                .build();
        return send(request, path);
    }

    /**
     * GET a public Hypixel endpoint that does not require an API key
     * (e.g. the Bazaar). Safe to call without the user configuring a key.
     */
    private CompletableFuture<Optional<JsonObject>> getJsonPublic(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .GET()
                .timeout(TIMEOUT)
                .build();
        return send(request, path);
    }

    private CompletableFuture<Optional<JsonObject>> send(HttpRequest request, String path) {
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        ReevesClient.LOGGER.warn("Hypixel API returned {}: {}", response.statusCode(), path);
                        return Optional.<JsonObject>empty();
                    }
                    try {
                        JsonElement el = JsonParser.parseString(response.body());
                        if (el.isJsonObject()) return Optional.of(el.getAsJsonObject());
                    } catch (JsonParseException e) {
                        ReevesClient.LOGGER.warn("Failed to parse Hypixel API response: {}", e.getMessage());
                    }
                    return Optional.<JsonObject>empty();
                })
                .exceptionally(e -> {
                    ReevesClient.LOGGER.warn("Hypixel API request failed: {}", e.getMessage());
                    return Optional.empty();
                });
    }

    // ── Endpoints ────────────────────────────────────────────────────────────

    /** Fetches basic player data by UUID. */
    public CompletableFuture<Optional<JsonObject>> fetchPlayer(String uuid) {
        return getJson("/player?uuid=" + uuid);
    }

    /** Fetches guild by player UUID. */
    public CompletableFuture<Optional<JsonObject>> fetchGuildByPlayer(String uuid) {
        return getJson("/guild?player=" + uuid);
    }

    /** Fetches SkyBlock profile list for a player. */
    public CompletableFuture<Optional<JsonObject>> fetchSkyBlockProfiles(String uuid) {
        return getJson("/skyblock/profiles?uuid=" + uuid);
    }

    /** Fetches SkyBlock Bazaar product list. */
    public CompletableFuture<Optional<JsonObject>> fetchBazaar() {
        return getJson("/skyblock/bazaar");
    }

    /** Fetches the SkyBlock Bazaar without requiring an API key (public endpoint). */
    public CompletableFuture<Optional<JsonObject>> fetchBazaarPublic() {
        return getJsonPublic("/skyblock/bazaar");
    }

    /** Fetches a page of Auction House data. */
    public CompletableFuture<Optional<JsonObject>> fetchAuctions(int page) {
        return getJson("/skyblock/auctions?page=" + page);
    }

    /** Fetches end-price Auction House data (completed auctions). */
    public CompletableFuture<Optional<JsonObject>> fetchAuctionsEnded() {
        return getJson("/skyblock/auctions_ended");
    }

    /** Fetches current Hypixel status (active events, etc.). */
    public CompletableFuture<Optional<JsonObject>> fetchCurrentStatus() {
        return getJson("/skyblock/election");
    }
}
