package com.reevesclient.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void afterRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // Reserved hook point for future 3D overlay rendering.
    }
}
