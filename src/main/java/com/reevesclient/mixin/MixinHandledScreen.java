package com.reevesclient.mixin;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.accessibility.ItemProtectionModule;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Item-protection guards for inventory menus:
 *  - hold the Reeves lock key and click an item to lock/unlock it,
 *  - cancels click actions that would drop/move a locked item,
 *  - draws a small padlock on locked slots.
 *
 * All client-side: only the player's own clicks are cancelled before they turn
 * into packets. Nothing is forged or auto-performed.
 */
@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {

    private ItemProtectionModule reeves$module() {
        ReevesClient rc = ReevesClient.getInstance();
        if (rc == null || rc.getModuleManager() == null) return null;
        ItemProtectionModule m = rc.getModuleManager().get(ItemProtectionModule.class);
        return (m != null && m.isEnabled()) ? m : null;
    }

    /** True while the (re-bindable) lock key is physically held. */
    private boolean reeves$lockKeyHeld() {
        int code = KeyBindingHelper.getBoundKeyOf(ReevesClient.KEY_TOGGLE_ITEM_LOCK).getCode();
        if (code == -1) return false;
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), code);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"), cancellable = true)
    private void reeves$guardLockedItems(Slot slot, int slotId, int button,
                                         SlotActionType actionType, CallbackInfo ci) {
        ItemProtectionModule m = reeves$module();
        if (m == null) return;

        ItemStack stack = slot != null ? slot.getStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) return;

        // Hold lock key + click toggles the lock instead of performing the action.
        if (reeves$lockKeyHeld()) {
            boolean nowLocked = m.toggleLock(stack);
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(nowLocked
                        ? "§eItem locked — protected from drop/sell."
                        : "§7Item unlocked."), true);
            }
            ci.cancel();
            return;
        }

        if (!m.isLocked(stack)) return;

        // Always block dropping/throwing a locked item.
        if (actionType == SlotActionType.THROW) {
            reeves$notifyBlocked();
            ci.cancel();
            return;
        }
        // Optionally block any movement (covers selling/salvaging/NPC menus).
        if (m.preventMovement()
                && (actionType == SlotActionType.QUICK_MOVE
                 || actionType == SlotActionType.PICKUP
                 || actionType == SlotActionType.PICKUP_ALL
                 || actionType == SlotActionType.SWAP)) {
            reeves$notifyBlocked();
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V",
            at = @At("TAIL"))
    private void reeves$drawLockIcon(DrawContext ctx, Slot slot, CallbackInfo ci) {
        ItemProtectionModule m = reeves$module();
        if (m == null || slot == null || !slot.hasStack() || !m.isLocked(slot.getStack())) return;
        // Tiny padlock at the slot's top-right corner.
        int x = slot.x + 10, y = slot.y - 1;
        ctx.fill(x,     y + 3, x + 7, y + 8, 0xFF1A1A1A); // body outline
        ctx.fill(x + 1, y + 4, x + 6, y + 7, 0xFFFFD54F); // body
        ctx.fill(x + 2, y,     x + 5, y + 4, 0xFF1A1A1A); // shackle
        ctx.fill(x + 3, y + 1, x + 4, y + 3, 0xFFFFD54F);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
    private void reeves$chestValue(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ReevesClient rc = ReevesClient.getInstance();
        if (rc == null || rc.getModuleManager() == null) return;
        var cv = rc.getModuleManager().get(com.reevesclient.modules.dungeons.DungeonChestValueModule.class);
        if (cv == null || !cv.isEnabled()) return;

        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        if (!cv.isChestTitle(self.getTitle().getString())) return;

        var econ = rc.getModuleManager().get(com.reevesclient.modules.skyblock.EconomyTooltipModule.class);
        if (econ == null) return;

        double total = 0;
        for (Slot s : self.getScreenHandler().slots) {
            ItemStack st = s.getStack();
            if (st.isEmpty()) continue;
            String id = com.reevesclient.core.util.HypixelUtil.getSkyblockId(st);
            if (id.isEmpty()) continue;
            total += econ.getSellPrice(id) * st.getCount();
        }
        if (total > 0) {
            com.reevesclient.core.util.RenderUtil.drawText(ctx,
                    "Chest value: ~" + com.reevesclient.core.util.Calculator.format(total) + " coins",
                    8, 8, 0xFFFFD54F);
        }
    }

    private void reeves$notifyBlocked() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§eThat item is locked. Hold your Reeves lock key and click it to unlock."), true);
        }
    }
}
