package com.reevesclient.modules.accessibility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;
import com.reevesclient.core.util.HypixelUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

/**
 * Client-side item protection. The user maintains a lock-list; locked items are
 * guarded against accidental dropping and (optionally) moving/selling/salvaging
 * in any menu. Lock state is keyed by SkyBlock id when present, else vanilla
 * registry id, so it follows the item type across stacks.
 *
 * SAFETY: purely a local input guard — it only cancels the player's own click
 * before it becomes a packet. It never forges packets or bypasses the server.
 */
public class ItemProtectionModule extends Module {

    private final Set<String> lockedKeys = new HashSet<>();

    private final BooleanSetting preventMovement;
    private final BooleanSetting showTooltip;

    public ItemProtectionModule() {
        super("item_protection", "Item Protection",
                "Lock items to prevent accidental dropping, selling or salvaging. Client-side only.",
                ModuleCategory.ACCESSIBILITY, true);
        preventMovement = addSetting(new BooleanSetting("prevent_movement", "Prevent Movement",
                "Also block moving/selling/salvaging locked items in any menu.", true));
        showTooltip = addSetting(new BooleanSetting("show_tooltip", "Lock Tooltip",
                "Show a lock line on protected items.", true));
    }

    @Override
    public void init() {
        // Append a lock indicator to protected items' tooltips.
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!isEnabled() || !showTooltip.getValue()) return;
            if (isLocked(stack)) {
                lines.add(Text.literal("[Locked] Reeves Client protection").formatted(Formatting.YELLOW));
            }
        });
    }

    /** Lock key for an item: SkyBlock id if present, else vanilla registry id. */
    public String keyFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        String sb = HypixelUtil.getSkyblockId(stack);
        if (!sb.isEmpty()) return "sb:" + sb;
        return "mc:" + Registries.ITEM.getId(stack.getItem());
    }

    public boolean isLocked(ItemStack stack) {
        String k = keyFor(stack);
        return !k.isEmpty() && lockedKeys.contains(k);
    }

    /** Toggles the lock for the item's type. Returns the new locked state. */
    public boolean toggleLock(ItemStack stack) {
        String k = keyFor(stack);
        if (k.isEmpty()) return false;
        boolean nowLocked;
        if (lockedKeys.contains(k)) { lockedKeys.remove(k); nowLocked = false; }
        else                        { lockedKeys.add(k);    nowLocked = true;  }
        save();
        return nowLocked;
    }

    public boolean preventMovement() { return preventMovement.getValue(); }

    private void save() {
        var cfg = ReevesClient.getInstance().getConfigManager();
        if (cfg != null) cfg.save();
    }

    // ── Persistence: store the lock-list alongside the module's settings ─────────

    @Override
    public JsonObject serialize() {
        JsonObject o = super.serialize();
        JsonArray arr = new JsonArray();
        for (String k : lockedKeys) arr.add(k);
        o.add("locked", arr);
        return o;
    }

    @Override
    public void deserialize(JsonObject o) {
        super.deserialize(o);
        lockedKeys.clear();
        if (o.has("locked") && o.get("locked").isJsonArray()) {
            for (JsonElement e : o.getAsJsonArray("locked")) {
                try { lockedKeys.add(e.getAsString()); } catch (Exception ignored) {}
            }
        }
    }
}
