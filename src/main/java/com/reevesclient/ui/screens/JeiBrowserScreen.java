package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.jei.JeiManager;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.recipe.RecipeView;
import com.reevesclient.ui.components.RButton;
import com.reevesclient.ui.components.RTextField;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * JEI-style item & recipe browser: searchable icon grid, recipe/usage viewer with
 * crafting-tree drill-down, favorites, recently-viewed, and history navigation.
 *
 * Recipes render from the bundled clean-room dataset (see RecipeRegistry). Showing
 * server-side / SkyBlock recipes that aren't in the dataset REQUIRES RUNTIME
 * VERIFICATION (recipe-book merge), and is wired through RecipeRegistry.mergeRuntimeRecipe.
 */
public class JeiBrowserScreen extends Screen {

    private enum Tab { ALL, FAVORITES, RECENT }

    private final Screen parent;
    private JeiManager jei;

    private final List<Item> allItems = new ArrayList<>();
    private String search = "";
    private int scrollOffset = 0;
    private Tab tab = Tab.ALL;

    private String selectedId = null;
    private int recipeIndex = 0;
    private boolean usageMode = false;

    private RTextField searchField;

    // Clickable icons captured during render (for navigation / selection).
    private final List<int[]> gridHits = new ArrayList<>();      // x,y,w,h
    private final List<Item>  gridItems = new ArrayList<>();
    private final List<int[]>    recipeHits = new ArrayList<>();  // x,y,w,h
    private final List<String>   recipeHitIds = new ArrayList<>();

    private static final int PADDING = 10;
    private static final int CELL = 18;
    private static final int LEFT_W = 540;

    public JeiBrowserScreen(Screen parent) {
        super(Text.literal("Item & Recipe Browser"));
        this.parent = parent;
        for (Item item : Registries.ITEM) {
            if (item != null && !new ItemStack(item).isEmpty()) allItems.add(item);
        }
        allItems.sort(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    protected void init() {
        jei = ReevesClient.getInstance().getJeiManager();
        int px = px(), py = py(), pw = pw(), ph = ph();

        searchField = new RTextField(px + PADDING, py + 40, LEFT_W - PADDING * 2, 18, "Search…  (use @namespace to filter by mod)");
        searchField.setChangedListener(s -> { search = s; scrollOffset = 0; });
        addDrawableChild(searchField);

        // Tabs
        addDrawableChild(new RButton(px + PADDING,        py + 64, 90, 18, "All",       b -> tab = Tab.ALL));
        addDrawableChild(new RButton(px + PADDING + 96,   py + 64, 90, 18, "Favorites", b -> tab = Tab.FAVORITES));
        addDrawableChild(new RButton(px + PADDING + 192,  py + 64, 90, 18, "Recent",    b -> tab = Tab.RECENT));

        // Recipe panel controls (right side)
        int rx = px + LEFT_W + PADDING;
        addDrawableChild(new RButton(rx,       py + ph - 30, 70, 22, "Recipe", b -> { usageMode = false; recipeIndex = 0; }));
        addDrawableChild(new RButton(rx + 76,  py + ph - 30, 70, 22, "Usage",  b -> { usageMode = true;  recipeIndex = 0; }));
        addDrawableChild(new RButton(rx + 152, py + ph - 30, 28, 22, "◀", b -> { String id = jei.goBack();    if (id != null) { selectedId = id; recipeIndex = 0; } }));
        addDrawableChild(new RButton(rx + 182, py + ph - 30, 28, 22, "▶", b -> { String id = jei.goForward(); if (id != null) { selectedId = id; recipeIndex = 0; } }));
        addDrawableChild(new RButton(rx + 212, py + ph - 30, 28, 22, "★", b -> { if (selectedId != null) jei.toggleFavorite(selectedId); }));

        addDrawableChild(new RButton(px + pw - 80, py + 8, 70, 20, "Close", b -> client.setScreen(parent)));
        // cycle recipe variants
        addDrawableChild(new RButton(rx, py + ph - 56, 240, 18, "Next variant", b -> recipeIndex++));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA000000);
        int px = px(), py = py(), pw = pw(), ph = ph();
        RenderUtil.fillRoundedRect(ctx, px, py, pw, ph, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, pw, ph, 1, ColorUtil.RC_BORDER);
        ctx.fill(px, py, px + pw, py + 32, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "✦ Item & Recipe Browser", px + PADDING, py + 11, ColorUtil.RC_ACCENT);

        ItemStack hovered = renderGrid(ctx, px, py, ph, mouseX, mouseY);
        renderRecipePanel(ctx, px + LEFT_W, py + 32, pw - LEFT_W, ph - 32, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);

        if (hovered != null && !hovered.isEmpty()) {
            ctx.drawItemTooltip(textRenderer, hovered, mouseX, mouseY);
        }
    }

    private ItemStack renderGrid(DrawContext ctx, int px, int py, int ph, int mouseX, int mouseY) {
        gridHits.clear();
        gridItems.clear();
        int gx = px + PADDING;
        int gy = py + 88;
        int gridH = ph - 88 - PADDING;
        int cols = Math.max(1, (LEFT_W - PADDING * 2) / CELL);
        ItemStack hovered = null;

        List<Item> items = filtered();
        int firstRow = scrollOffset / CELL;
        for (int i = 0; i < items.size(); i++) {
            int r = i / cols;
            if (r < firstRow) continue;
            int cx = gx + (i % cols) * CELL;
            int cy = gy + (r - firstRow) * CELL;
            if (cy > gy + gridH - CELL) break;
            Item item = items.get(i);
            ItemStack stack = new ItemStack(item);
            boolean over = mouseX >= cx && mouseX < cx + 16 && mouseY >= cy && mouseY < cy + 16;
            if (over) { ctx.fill(cx - 1, cy - 1, cx + 17, cy + 17, 0x55FFFFFF); hovered = stack; }
            if (item == itemFor(selectedId)) ctx.fill(cx - 1, cy - 1, cx + 17, cy + 17, ColorUtil.withAlpha(ColorUtil.RC_ACCENT, 0x66));
            ctx.drawItem(stack, cx, cy);
            gridHits.add(new int[]{cx, cy, 16, 16});
            gridItems.add(item);
        }

        RenderUtil.drawText(ctx, items.size() + " items", gx, py + ph - PADDING - 8, ColorUtil.RC_TEXT_MUTED);
        return hovered;
    }

    private void renderRecipePanel(DrawContext ctx, int x, int y, int w, int h, int mouseX, int mouseY) {
        ctx.fill(x, y, x + w, y + h, ColorUtil.RC_BG_PANEL);
        recipeHits.clear();
        recipeHitIds.clear();
        if (selectedId == null) {
            RenderUtil.drawText(ctx, "Click an item to view recipes.", x + PADDING, y + PADDING, ColorUtil.RC_TEXT_MUTED);
            return;
        }

        ItemStack sel = stackFor(selectedId);
        ctx.drawItem(sel, x + PADDING, y + PADDING);
        RenderUtil.drawText(ctx, sel.getName().getString(), x + PADDING + 22, y + PADDING + 1, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, selectedId, x + PADDING + 22, y + PADDING + 12, ColorUtil.RC_TEXT_MUTED);
        if (jei.isFavorite(selectedId)) RenderUtil.drawText(ctx, "★", x + w - PADDING - 8, y + PADDING + 1, ColorUtil.RC_WARNING);

        List<RecipeView> recipes = usageMode
                ? jei.getRegistry().getUsagesOf(selectedId)
                : jei.getRegistry().getRecipesFor(selectedId);

        int ry = y + PADDING + 30;
        RenderUtil.drawText(ctx, (usageMode ? "Used in" : "Recipe") + ": "
                + (recipes.isEmpty() ? "none in dataset" : (recipeIndex % recipes.size() + 1) + "/" + recipes.size()),
                x + PADDING, ry, ColorUtil.RC_ACCENT);
        ry += 14;

        if (recipes.isEmpty()) {
            RenderUtil.drawText(ctx, usageMode ? "No known uses." : "No recipe in the bundled dataset.",
                    x + PADDING, ry, ColorUtil.RC_TEXT_MUTED);
            RenderUtil.drawText(ctx, "(SkyBlock/server recipes: runtime verification)",
                    x + PADDING, ry + 12, ColorUtil.RC_TEXT_MUTED);
            return;
        }

        RecipeView recipe = recipes.get(recipeIndex % recipes.size());
        RenderUtil.drawText(ctx, "[" + recipe.getCategory().displayName + "]", x + w - PADDING - 70, ry - 14, ColorUtil.RC_TEXT_MUTED);

        // 3×3 ingredient grid
        int gx = x + PADDING, gy = ry + 4;
        String[] grid = recipe.getGrid();
        for (int i = 0; i < 9; i++) {
            int cx = gx + (i % 3) * 20;
            int cy = gy + (i / 3) * 20;
            RenderUtil.drawBorder(ctx, cx - 1, cy - 1, 18, 18, 1, ColorUtil.RC_BORDER);
            ItemStack ing = stackFor(grid[i]);
            if (!ing.isEmpty()) {
                ctx.drawItem(ing, cx, cy);
                recipeHits.add(new int[]{cx, cy, 16, 16});
                recipeHitIds.add(grid[i]);
            }
        }
        // arrow + result
        int arrowX = gx + 64, resultX = gx + 84, resultY = gy + 20;
        RenderUtil.drawText(ctx, "→", arrowX, resultY + 4, ColorUtil.RC_TEXT);
        ItemStack result = stackFor(recipe.getResultId());
        RenderUtil.drawBorder(ctx, resultX - 1, resultY - 1, 18, 18, 1, ColorUtil.RC_ACCENT);
        ctx.drawItem(result, resultX, resultY);
        if (recipe.getResultCount() > 1) {
            RenderUtil.drawText(ctx, "x" + recipe.getResultCount(), resultX + 18, resultY + 5, ColorUtil.RC_TEXT);
        }
        recipeHits.add(new int[]{resultX, resultY, 16, 16});
        recipeHitIds.add(recipe.getResultId());

        RenderUtil.drawText(ctx, "Click any item to drill into its recipe.", x + PADDING, gy + 66, ColorUtil.RC_TEXT_MUTED);

        // hovered ingredient tooltip
        for (int i = 0; i < recipeHits.size(); i++) {
            int[] r = recipeHits.get(i);
            if (mouseX >= r[0] && mouseX < r[0] + r[2] && mouseY >= r[1] && mouseY < r[1] + r[3]) {
                ctx.drawItemTooltip(textRenderer, stackFor(recipeHitIds.get(i)), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        if (super.mouseClicked(click, shifted)) return true;
        double mx = click.x(), my = click.y();

        // Grid item click → select + navigate.
        for (int i = 0; i < gridHits.size(); i++) {
            int[] r = gridHits.get(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                select(idFor(gridItems.get(i)));
                return true;
            }
        }
        // Recipe icon click → drill into that item.
        for (int i = 0; i < recipeHits.size(); i++) {
            int[] r = recipeHits.get(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                select(recipeHitIds.get(i));
                return true;
            }
        }
        return false;
    }

    private void select(String id) {
        if (id == null) return;
        selectedId = id;
        recipeIndex = 0;
        jei.navigateTo(id);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scrollOffset = Math.max(0, scrollOffset - (int) (v * CELL));
        return true;
    }

    private List<Item> filtered() {
        List<Item> base = switch (tab) {
            case FAVORITES -> idsToItems(jei.getFavorites());
            case RECENT    -> idsToItems(jei.getRecent());
            default        -> allItems;
        };
        if (search.isBlank()) return base;
        String q = search.toLowerCase(Locale.ROOT).trim();
        boolean byMod = q.startsWith("@");
        String needle = byMod ? q.substring(1) : q;
        List<Item> out = new ArrayList<>();
        for (Item it : base) {
            String id = idFor(it).toLowerCase(Locale.ROOT);
            if (byMod) {
                String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : id;
                if (namespace.contains(needle)) out.add(it);
            } else if (displayName(it).toLowerCase(Locale.ROOT).contains(needle) || id.contains(needle)) {
                out.add(it);
            }
        }
        return out;
    }

    private List<Item> idsToItems(java.util.Collection<String> ids) {
        List<Item> out = new ArrayList<>();
        for (String id : ids) { Item it = itemFor(id); if (it != null) out.add(it); }
        return out;
    }

    private ItemStack stackFor(String id) {
        Item it = itemFor(id);
        return it == null ? ItemStack.EMPTY : new ItemStack(it);
    }

    private Item itemFor(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            Item it = Registries.ITEM.get(Identifier.of(id));
            return new ItemStack(it).isEmpty() ? null : it;
        } catch (Exception e) { return null; }
    }

    private String idFor(Item it) {
        Identifier id = Registries.ITEM.getId(it);
        return id == null ? "" : id.toString();
    }

    private String displayName(Item it) { return new ItemStack(it).getName().getString(); }

    private int px() { return Math.max(10, (width - 920) / 2); }
    private int py() { return Math.max(10, (height - 560) / 2); }
    private int pw() { return Math.min(width - 20, 920); }
    private int ph() { return Math.min(height - 20, 560); }

    @Override public boolean shouldPause() { return false; }
}
