package com.reevesclient.mixin;

import com.reevesclient.modules.performance.EntityRenderModule;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

    /**
     * Optionally overrides shouldRender to apply our custom entity distance culling.
     */
    @Inject(method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
            at = @At("RETURN"), cancellable = true)
    private void onShouldRender(T entity, Frustum frustum, double camX, double camY, double camZ,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // already culled, nothing to change
        if (!EntityRenderModule.limitInvisibleEntities) return;

        if (entity.isInvisible()) {
            double dist = entity.squaredDistanceTo(camX, camY, camZ);
            double limit = 16.0 * 16.0; // 16 block radius for invisible entities
            if (dist > limit) cir.setReturnValue(false);
        }
    }
}
