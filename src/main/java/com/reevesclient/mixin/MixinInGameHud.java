package com.reevesclient.mixin;

import com.reevesclient.ReevesClient;
import com.reevesclient.modules.pvp.CrosshairModule;
import com.reevesclient.modules.skyblock.GardenDashboardModule;
import com.reevesclient.modules.skyblock.SkillTrackerModule;
import com.reevesclient.modules.utility.ChatEnhancementsModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    /** Intercept action-bar messages so skill/garden parsers can read them. */
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message == null || ReevesClient.getInstance() == null) return;
        String plain = com.reevesclient.core.util.TextUtil.stripFormatting(message.getString());

        var mm = ReevesClient.getInstance().getModuleManager();
        if (mm == null) return;

        SkillTrackerModule stm = mm.get(SkillTrackerModule.class);
        if (stm != null) stm.parseActionBar(plain);

        GardenDashboardModule gdm = mm.get(GardenDashboardModule.class);
        if (gdm != null) gdm.parseActionBar(plain);

        com.reevesclient.modules.skyblock.SkyblockStatsModule ssm =
                mm.get(com.reevesclient.modules.skyblock.SkyblockStatsModule.class);
        if (ssm != null) ssm.parseActionBar(plain);
    }

    /** Optionally hide vanilla health/hunger/armor bars when RPG bars are active. */
    @Inject(method = "renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("HEAD"), cancellable = true)
    private void onRenderStatusBars(DrawContext ctx, CallbackInfo ci) {
        if (ReevesClient.getInstance() == null) return;
        var mm = ReevesClient.getInstance().getModuleManager();
        if (mm == null) return;
        com.reevesclient.modules.skyblock.SkyblockStatsModule ssm =
                mm.get(com.reevesclient.modules.skyblock.SkyblockStatsModule.class);
        if (ssm != null && ssm.isEnabled() && ssm.hideVanillaBars()
                && com.reevesclient.core.util.HypixelUtil.isInSkyBlock()) {
            ci.cancel();
        }
    }

    /** Suppress vanilla crosshair rendering when the custom one is active. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!CrosshairModule.shouldHideVanilla) return;

        var mm = ReevesClient.getInstance() != null ? ReevesClient.getInstance().getModuleManager() : null;
        if (mm == null) return;

        CrosshairModule cm = mm.get(CrosshairModule.class);
        if (cm != null && cm.isEnabled()) {
            cm.render(ctx);
            ci.cancel(); // suppress vanilla crosshair
        }
    }
}
