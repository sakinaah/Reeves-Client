package com.reevesclient.cape;

import com.reevesclient.ReevesClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Renders the Reeves Client custom cape on the local player.
 * Rendering logic mirrors vanilla cape rendering — only a texture swap.
 *
 * NOTE: This only renders for the local player in third-person or for players
 * viewing the cape screen. For multiplayer visibility between Reeves Client users,
 * a future server-side component would be needed — not implemented here.
 */
public final class CapeRenderer {

    private CapeRenderer() {}

    /**
     * Returns the cape texture identifier to use for a player, or null if no Reeves cape applies.
     * Called from MixinPlayerEntityRenderer during cape rendering.
     */
    public static Identifier getOverrideTexture(AbstractClientPlayerEntity player) {
        CapeManager cm = ReevesClient.getInstance().getCapeManager();
        if (cm == null) return null;
        CapeProfile active = cm.getActiveProfile();
        if (active == null || !active.hasTexture()) return null;

        // Only render on the local player (self-only or same-client players)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.getUuid().equals(player.getUuid())) return null;

        // Visibility check
        if (active.getVisibility() == CapeProfile.Visibility.SELF_ONLY && mc.gameRenderer.getCamera() != null) {
            if (mc.options.getPerspective() == net.minecraft.client.option.Perspective.FIRST_PERSON) {
                return null; // don't render in first person — no visible difference, avoids confusion
            }
        }

        return active.getTextureId();
    }

    /**
     * Returns true if Reeves Client is providing a cape override for the given player.
     * Replaces whatever vanilla cape (Optifine/Mojang) would have been used.
     */
    public static boolean shouldOverrideCape(AbstractClientPlayerEntity player) {
        return getOverrideTexture(player) != null;
    }
}
