package com.reevesclient.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reevesclient.ReevesClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all known {@link RecipeView}s, indexed by result and by ingredient so the
 * browser can answer both "how do I make X" (recipes) and "what uses X" (usages).
 *
 * The verifiable core is a bundled clean-room dataset
 * ({@code data/reeves-client/recipes/recipes.json}). Recipes the client recipe
 * book exposes can additionally be merged at runtime via {@link #mergeRuntimeRecipe}
 * — REQUIRES RUNTIME VERIFICATION (the 1.21.10 recipe-display API only yields
 * unlocked recipes and varies by server).
 */
public final class RecipeRegistry {

    private final List<RecipeView> all = new ArrayList<>();
    private final Map<String, List<RecipeView>> byResult = new HashMap<>();
    private final Map<String, List<RecipeView>> byIngredient = new HashMap<>();
    private boolean loaded = false;

    public void load() {
        if (loaded) return;
        loaded = true;
        try (InputStream is = getClass().getResourceAsStream("/data/reeves-client/recipes/recipes.json")) {
            if (is == null) {
                ReevesClient.LOGGER.warn("No bundled recipe dataset found.");
                return;
            }
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String result = o.get("result").getAsString();
                int count = o.has("count") ? o.get("count").getAsInt() : 1;
                RecipeCategory cat = RecipeCategory.fromString(o.has("category") ? o.get("category").getAsString() : "CRAFTING");
                String[] grid = new String[9];
                if (o.has("grid")) {
                    JsonArray g = o.getAsJsonArray("grid");
                    for (int i = 0; i < 9 && i < g.size(); i++) {
                        JsonElement ge = g.get(i);
                        grid[i] = (ge == null || ge.isJsonNull()) ? null : ge.getAsString();
                    }
                }
                index(new RecipeView(result, count, grid, cat));
            }
            ReevesClient.LOGGER.info("RecipeRegistry: loaded {} recipes.", all.size());
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load recipe dataset: {}", e.getMessage());
        }
    }

    private void index(RecipeView r) {
        all.add(r);
        byResult.computeIfAbsent(r.getResultId(), k -> new ArrayList<>()).add(r);
        for (String ing : r.getGrid()) {
            if (ing == null) continue;
            List<RecipeView> list = byIngredient.computeIfAbsent(ing, k -> new ArrayList<>());
            if (!list.contains(r)) list.add(r);
        }
    }

    /**
     * Merges a recipe discovered at runtime (e.g. from the client recipe book).
     * REQUIRES RUNTIME VERIFICATION — call site must be guarded.
     */
    public void mergeRuntimeRecipe(RecipeView r) {
        if (r == null) return;
        index(r);
    }

    /** Recipes that produce the given item id. */
    public List<RecipeView> getRecipesFor(String resultId) {
        return Collections.unmodifiableList(byResult.getOrDefault(resultId, List.of()));
    }

    /** Recipes that consume the given item id. */
    public List<RecipeView> getUsagesOf(String ingredientId) {
        return Collections.unmodifiableList(byIngredient.getOrDefault(ingredientId, List.of()));
    }

    public boolean hasRecipe(String resultId)   { return byResult.containsKey(resultId); }
    public boolean hasUsage(String ingredientId){ return byIngredient.containsKey(ingredientId); }
    public int     size()                       { return all.size(); }
}
