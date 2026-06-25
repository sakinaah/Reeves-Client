package com.reevesclient.core.render;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.ModuleManager;
import com.reevesclient.modules.utility.WaypointModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * In-world 3D rendering through Fabric's {@link WorldRenderEvents} — the
 * replacement for the old {@code WorldRenderer} mixin, which was a no-op stub
 * after the 1.21.10 render-pipeline overhaul.
 *
 * Currently draws {@link WaypointModule} markers (absolute world coordinates, so
 * they need no dungeon room detection). Dungeon secret boxes will reuse this once
 * map-item scanning provides room origins.
 *
 * The whole callback is wrapped so a render exception can never crash the frame.
 */
public final class WorldRenderer3D {

    private WorldRenderer3D() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderer3D::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        try {
            ReevesClient rc = ReevesClient.getInstance();
            if (rc == null || rc.getModuleManager() == null) return;
            ModuleManager mm = rc.getModuleManager();

            WaypointModule wm = mm.get(WaypointModule.class);
            if (wm == null || !wm.isEnabled() || wm.getWaypoints().isEmpty()) return;

            MatrixStack matrices = context.matrices();
            VertexConsumerProvider consumers = context.consumers();
            if (matrices == null || consumers == null) return;

            // Per the Fabric contract, geometry must be camera-relative.
            Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

            for (WaypointModule.Waypoint wp : wm.getWaypoints()) {
                if (!wp.visible) continue;
                float r = ((wp.color >> 16) & 0xFF) / 255f;
                float g = ((wp.color >> 8)  & 0xFF) / 255f;
                float b = ( wp.color        & 0xFF) / 255f;

                matrices.push();
                matrices.translate(wp.x - cam.x, wp.y - cam.y, wp.z - cam.z);
                VertexRendering.drawBox(matrices.peek(), lines,
                        0.0, 0.0, 0.0, 1.0, 1.0, 1.0, r, g, b, 1.0f);
                matrices.pop();
            }
        } catch (Exception e) {
            ReevesClient.LOGGER.debug("World render skipped: {}", e.getMessage());
        }
    }
}
