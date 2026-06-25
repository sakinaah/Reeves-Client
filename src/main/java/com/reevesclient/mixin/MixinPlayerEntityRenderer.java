package com.reevesclient.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Cape texture injection stub for 1.21.10.
 * The SkinTextures API was redesigned in 1.21.10 (using AssetInfo$TextureAsset instead
 * of bare Identifiers). The cape override feature will be re-implemented in a future update.
 */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinPlayerEntityRenderer {
    // Cape injection disabled — pending 1.21.10 SkinTextures API port.
}
