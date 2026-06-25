package com.reevesclient.mixin;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.event.EventBus;
import com.reevesclient.core.event.events.ChatReceiveEvent;
import com.reevesclient.modules.dungeons.SecretWaypointModule;
import com.reevesclient.modules.skyblock.SlayerTrackerModule;
import com.reevesclient.modules.utility.ChatEnhancementsModule;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinChatScreen {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (message == null || ReevesClient.getInstance() == null) return;
        String plain = com.reevesclient.core.util.TextUtil.stripFormatting(message.getString());

        // Fire event (other listeners can cancel if needed)
        ChatReceiveEvent event = new ChatReceiveEvent(message);
        EventBus.getInstance().post(event);
        if (event.isCancelled()) { ci.cancel(); return; }

        var mm = ReevesClient.getInstance().getModuleManager();
        if (mm == null) return;

        // Chat enhancements — duplicate filter
        ChatEnhancementsModule cem = mm.get(ChatEnhancementsModule.class);
        if (cem != null && cem.onReceiveMessage(plain)) { ci.cancel(); return; }

        // Slayer tracker
        SlayerTrackerModule stm = mm.get(SlayerTrackerModule.class);
        if (stm != null) stm.onChat(plain);

        // Dungeon tracker — blood door / run completion
        com.reevesclient.modules.dungeons.DungeonScoreModule dsm =
                mm.get(com.reevesclient.modules.dungeons.DungeonScoreModule.class);
        if (dsm != null) dsm.onChat(plain);

        com.reevesclient.modules.dungeons.DungeonBossModule dbm =
                mm.get(com.reevesclient.modules.dungeons.DungeonBossModule.class);
        if (dbm != null) dbm.onChat(plain);

        // Secret waypoints — detect "You found a Secret!" line
        SecretWaypointModule swm = mm.get(SecretWaypointModule.class);
        if (swm != null && plain.contains("You found a Secret")) {
            // Mark the most recent waypoint in the current room as completed
            // (simplified — a full implementation would correlate proximity)
            swm.markCompleted("recent");
        }
    }
}
