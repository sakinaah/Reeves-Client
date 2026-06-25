package com.reevesclient.mixin;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.accessibility.HotbarLockModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {

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

        HotbarLockModule module = instance.getModuleManager().get(HotbarLockModule.class);
        if (module == null || !module.isEnabled()) {
            return;
        }

        int selectedSlot = client.player.getInventory().getSelectedSlot();
        if (module.isSlotLocked(selectedSlot)) {
            cir.setReturnValue(false);
        }
    }
}