package com.reevesclient.recipe;

/**
 * A renderable recipe: a 3×3 ingredient grid producing a result. Vanilla crafting
 * maps directly; smelting/SkyBlock recipes use the centre slot for the single
 * input. Ingredient slots are item ids ("minecraft:stick") or null/empty.
 *
 * Recipes come from the bundled clean-room dataset and (at runtime) may be merged
 * with recipes the client recipe book exposes — see {@link RecipeRegistry}.
 */
public final class RecipeView {

    private final String resultId;
    private final int resultCount;
    private final String[] grid;          // length 9, row-major; "" or null = empty
    private final RecipeCategory category;

    public RecipeView(String resultId, int resultCount, String[] grid, RecipeCategory category) {
        this.resultId    = resultId;
        this.resultCount = Math.max(1, resultCount);
        this.grid        = normalize(grid);
        this.category    = category == null ? RecipeCategory.CRAFTING : category;
    }

    private static String[] normalize(String[] in) {
        String[] out = new String[9];
        if (in != null) {
            for (int i = 0; i < 9 && i < in.length; i++) {
                out[i] = (in[i] == null || in[i].isBlank()) ? null : in[i].trim();
            }
        }
        return out;
    }

    public String        getResultId()    { return resultId; }
    public int           getResultCount() { return resultCount; }
    public String[]      getGrid()        { return grid; }
    public RecipeCategory getCategory()   { return category; }

    /** True if this recipe uses the given ingredient id anywhere in its grid. */
    public boolean usesIngredient(String id) {
        if (id == null) return false;
        for (String g : grid) if (id.equals(g)) return true;
        return false;
    }
}
