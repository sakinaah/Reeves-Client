package com.reevesclient.core.jei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reevesclient.ReevesClient;
import com.reevesclient.recipe.RecipeRegistry;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * State for the JEI-style browser: the recipe registry, persisted favorites and
 * recently-viewed items, and an in-session recipe navigation history (back/forward
 * like a browser). Pure client state — no server interaction.
 */
public class JeiManager {

    private static final int MAX_RECENT = 30;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final RecipeRegistry registry = new RecipeRegistry();
    private final Set<String> favorites = new LinkedHashSet<>();
    private final Deque<String> recent = new ArrayDeque<>();

    // Recipe navigation history (item ids the user drilled into).
    private final java.util.List<String> history = new java.util.ArrayList<>();
    private int historyIndex = -1;

    private final Path saveFile;

    public JeiManager() {
        saveFile = FabricLoader.getInstance().getConfigDir().resolve("reeves-client/jei.json");
    }

    public void init() {
        registry.load();
        load();
    }

    public RecipeRegistry getRegistry() { return registry; }

    // ── Favorites ──────────────────────────────────────────────────────────────
    public boolean isFavorite(String id)  { return favorites.contains(id); }
    public Set<String> getFavorites()     { return new LinkedHashSet<>(favorites); }
    public void toggleFavorite(String id) {
        if (id == null || id.isEmpty()) return;
        if (!favorites.remove(id)) favorites.add(id);
        save();
    }

    // ── Recently viewed ─────────────────────────────────────────────────────────
    public java.util.List<String> getRecent() { return new java.util.ArrayList<>(recent); }
    public void pushRecent(String id) {
        if (id == null || id.isEmpty()) return;
        recent.remove(id);
        recent.addFirst(id);
        while (recent.size() > MAX_RECENT) recent.removeLast();
        save();
    }

    // ── Recipe navigation history ───────────────────────────────────────────────
    public void navigateTo(String id) {
        if (id == null || id.isEmpty()) return;
        // Drop any forward history when navigating to a new item.
        while (history.size() > historyIndex + 1) history.remove(history.size() - 1);
        history.add(id);
        historyIndex = history.size() - 1;
        pushRecent(id);
    }
    public boolean canGoBack()    { return historyIndex > 0; }
    public boolean canGoForward() { return historyIndex >= 0 && historyIndex < history.size() - 1; }
    public String goBack()    { if (canGoBack())    { historyIndex--; return history.get(historyIndex); } return null; }
    public String goForward() { if (canGoForward()) { historyIndex++; return history.get(historyIndex); } return null; }
    public String current()   { return (historyIndex >= 0 && historyIndex < history.size()) ? history.get(historyIndex) : null; }

    // ── Persistence ─────────────────────────────────────────────────────────────
    private void load() {
        if (!Files.exists(saveFile)) return;
        try (Reader r = new InputStreamReader(Files.newInputStream(saveFile), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            favorites.clear();
            recent.clear();
            if (root.has("favorites")) for (JsonElement e : root.getAsJsonArray("favorites")) favorites.add(e.getAsString());
            if (root.has("recent"))    for (JsonElement e : root.getAsJsonArray("recent"))    recent.addLast(e.getAsString());
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load jei.json: {}", e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(saveFile.getParent());
            JsonObject root = new JsonObject();
            JsonArray fav = new JsonArray(); favorites.forEach(fav::add);
            JsonArray rec = new JsonArray(); recent.forEach(rec::add);
            root.add("favorites", fav);
            root.add("recent", rec);
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(saveFile), StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to save jei.json: {}", e.getMessage());
        }
    }
}
