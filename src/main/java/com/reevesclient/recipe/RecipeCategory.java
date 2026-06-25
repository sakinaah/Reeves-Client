package com.reevesclient.recipe;

/** Categories a recipe can belong to, for the JEI-style recipe viewer. */
public enum RecipeCategory {
    CRAFTING("Crafting"),
    SMELTING("Smelting"),
    SKYBLOCK("SkyBlock"),
    FORGE("Forge"),
    OTHER("Other");

    public final String displayName;

    RecipeCategory(String displayName) { this.displayName = displayName; }

    public static RecipeCategory fromString(String s) {
        if (s == null) return CRAFTING;
        try { return valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return OTHER; }
    }
}
