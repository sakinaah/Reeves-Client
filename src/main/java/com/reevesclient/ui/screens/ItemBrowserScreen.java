package com.reevesclient.ui.screens;

import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.RButton;
import com.reevesclient.ui.components.RTextField;
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
 * Client-side item browser with registry search.
 * This is a safe local lookup tool; it does not automate any server action.
 */
public class ItemBrowserScreen extends Screen {

    private final Screen parent;
    private final List<Item> items = new ArrayList<>();
    private String searchText = "";
    private int scrollOffset = 0;
    private Item selectedItem = null;
    private RTextField searchField;

    private static final int PADDING = 10;
    private static final int LEFT_W = 260;
    private static final int ITEM_H = 28;

    public ItemBrowserScreen(Screen parent) {
        super(Text.literal("Item Browser"));
        this.parent = parent;
        for (Item item : Registries.ITEM) {
            items.add(item);
        }
        items.sort(Comparator.comparing(this::getDisplayName, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    protected void init() {
        int px = getPanelX();
        int py = getPanelY();
        searchField = new RTextField(px + PADDING, py + 40, LEFT_W - PADDING * 2, 18, "Search items…");
        searchField.setChangedListener(text -> {
            searchText = text;
            scrollOffset = 0;
            selectedItem = null;
        });
        addDrawableChild(searchField);

        addDrawableChild(new RButton(px + getPanelW() - 90, py + getPanelH() - 30, 80, 22, "Back",
                b -> client.setScreen(parent)));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA000000);

        int px = getPanelX(), py = getPanelY(), pw = getPanelW(), ph = getPanelH();
        RenderUtil.fillRoundedRect(ctx, px, py, pw, ph, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, pw, ph, 1, ColorUtil.RC_BORDER);

        ctx.fill(px, py, px + pw, py + 34, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "Item Browser", px + PADDING, py + 11, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, "Search local registry entries", px + 120, py + 11, ColorUtil.RC_TEXT_MUTED);

        renderList(ctx, px, py + 34, ph - 34);
        renderDetails(ctx, px + LEFT_W, py + 34, pw - LEFT_W, ph - 34);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderList(DrawContext ctx, int x, int y, int h) {
        ctx.fill(x, y, x + LEFT_W, y + h, ColorUtil.RC_BG_PANEL);
        List<Item> filtered = getFilteredItems();

        int listTop = y + 28;
        for (int i = 0; i < filtered.size(); i++) {
            int itemY = listTop + i * ITEM_H - scrollOffset;
            if (itemY + ITEM_H < y || itemY > y + h) {
                continue;
            }

            Item item = filtered.get(i);
            boolean selected = item == selectedItem;
            if (selected) {
                ctx.fill(x, itemY, x + LEFT_W, itemY + ITEM_H, ColorUtil.RC_SURFACE);
            }

            RenderUtil.drawText(ctx, getDisplayName(item), x + PADDING, itemY + 8,
                    selected ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED);
        }
    }

    private void renderDetails(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, ColorUtil.RC_BG_PANEL);
        if (selectedItem == null) {
            RenderUtil.drawText(ctx, "Select an item to inspect it.", x + PADDING, y + PADDING, ColorUtil.RC_TEXT_MUTED);
            return;
        }

        Identifier id = Registries.ITEM.getId(selectedItem);
        ItemStack stack = new ItemStack(selectedItem);

        RenderUtil.drawText(ctx, getDisplayName(selectedItem), x + PADDING, y + PADDING, ColorUtil.RC_TEXT);
        RenderUtil.drawText(ctx, id != null ? id.toString() : "unknown", x + PADDING, y + PADDING + 14, ColorUtil.RC_TEXT_MUTED);
        RenderUtil.drawText(ctx, "Stack: " + stack.getMaxCount() + " max", x + PADDING, y + PADDING + 28, ColorUtil.RC_ACCENT);
        RenderUtil.drawText(ctx, "Crafting recipe lookup is not yet wired to the new recipe display API.",
                x + PADDING, y + PADDING + 46, ColorUtil.RC_TEXT_MUTED);
        RenderUtil.drawText(ctx, "Use this browser to find the exact item first, then open the vanilla recipe book.",
                x + PADDING, y + PADDING + 58, ColorUtil.RC_TEXT_MUTED);
    }

    private List<Item> getFilteredItems() {
        if (searchText.isBlank()) {
            return items;
        }

        String q = searchText.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(item -> getDisplayName(item).toLowerCase(Locale.ROOT).contains(q)
                        || getItemId(item).contains(q))
                .toList();
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean shifted) {
        double mx = click.x();
        double my = click.y();

        int px = getPanelX(), py = getPanelY();
        int listTop = py + 34 + 28;
        List<Item> filtered = getFilteredItems();
        for (int i = 0; i < filtered.size(); i++) {
            int itemY = listTop + i * ITEM_H - scrollOffset;
            if (mx >= px && mx <= px + LEFT_W && my >= itemY && my <= itemY + ITEM_H) {
                selectedItem = filtered.get(i);
                return true;
            }
        }

        return super.mouseClicked(click, shifted);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, getFilteredItems().size() * ITEM_H - (getPanelH() - 60));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 12)));
        return true;
    }

    private String getDisplayName(Item item) {
        return new ItemStack(item).getName().getString();
    }

    private String getItemId(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
    }

    private int getPanelX() { return Math.max(10, (width - 920) / 2); }
    private int getPanelY() { return Math.max(10, (height - 560) / 2); }
    private int getPanelW() { return Math.min(width - 20, 920); }
    private int getPanelH() { return Math.min(height - 20, 560); }

    @Override
    public boolean shouldPause() { return false; }
}