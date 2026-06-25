package com.reevesclient.mixin;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.accessibility.HotbarLockModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side drop protection.
 *
 * {@code dropSelectedItem(boolean)} is declared on {@link ClientPlayerEntity}
 * (it overrides PlayerEntity to send the drop packet) and is what runs when the
 * player presses the drop key. We cancel it for hotbar slots the user has locked.
 *
 * This is purely a local safety guard — no packets are sent and no server-side
 * behaviour is bypassed, so it is fully Hypixel-rule compliant.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {

    @Inject(method = "dropSelectedItem(Z)Z", at = @At("HEAD"), cancellable = true)
    private void reevesclient$preventLockedHotbarDrops(boolean entireStack,
                                                       CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        ReevesClient instance = ReevesClient.getInstance();
        if (instance == null || instance.getModuleManager() == null) {
            return;
        }

        // 1) Hotbar slot lock (HotbarLockModule).
        HotbarLockModule slotLock = instance.getModuleManager().get(HotbarLockModule.class);
        if (slotLock != null && slotLock.isEnabled()
                && slotLock.isSlotLocked(client.player.getInventory().getSelectedSlot())) {
            cir.setReturnValue(false); // matches vanilla's "nothing dropped"
            return;
        }

        // 2) Item-type lock (ItemProtectionModule) on the held stack.
        com.reevesclient.modules.accessibility.ItemProtectionModule prot =
                instance.getModuleManager().get(com.reevesclient.modules.accessibility.ItemProtectionModule.class);
        if (prot != null && prot.isEnabled() && prot.isLocked(client.player.getMainHandStack())) {
            cir.setReturnValue(false);
        }
    }
}
