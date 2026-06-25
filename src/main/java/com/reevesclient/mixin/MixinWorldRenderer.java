package com.reevesclient.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * World-space waypoint beacon rendering stub for 1.21.10.
 *
 * WorldRenderer.render() was completely rewritten in the 1.21.10 render-pipeline
 * overhaul (FrameGraph / GpuBufferSlice / RenderPipelines), so the old injection
 * point no longer exists. 3D beacon rendering will be re-implemented via Fabric's
 * WorldRenderEvents API in a future update. Waypoint data/management still works;
 * only the in-world beacon visuals are temporarily disabled.
 */
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    // Beacon injection disabled — pending 1.21.10 render-pipeline port.
}
