package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.core.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.*;

public class PotionEffectsHUD extends HUDElement {

    private record EffectData(String name, int durationTicks, int amplifier, boolean beneficial) {}

    private final List<EffectData> effects = new ArrayList<>();

    public PotionEffectsHUD() {
        super("potion_effects", "Potion Effects", 0.01f, 0.3f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        effects.clear();
        if (client.player == null) return;
        for (StatusEffectInstance instance : client.player.getStatusEffects()) {
            RegistryEntry<StatusEffect> effectEntry = instance.getEffectType();
            String name = Text.translatable(effectEntry.value().getTranslationKey()).getString();
            effects.add(new EffectData(
                name,
                instance.getDuration(),
                instance.getAmplifier(),
                effectEntry.value().isBeneficial()
            ));
        }
        effects.sort(Comparator.comparing(e -> e.name));
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        if (effects.isEmpty()) return;

        int y = 0;
        for (EffectData e : effects) {
            int color = e.beneficial ? ColorUtil.RC_SUCCESS : ColorUtil.RC_ERROR;
            String amplStr = e.amplifier > 0 ? " " + toRoman(e.amplifier + 1) : "";
            String dur     = formatDuration(e.durationTicks);
            String line    = e.name + amplStr + " (" + dur + ")";
            RenderUtil.drawText(ctx, line, 0, y, applyOpacity(color));
            y += 10;
        }
    }

    private String formatDuration(int ticks) {
        if (ticks == 32767) return "∞"; // infinite duration
        return TextUtil.formatClock(ticks / 20L);
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            case 9 -> "IX"; case 10 -> "X"; default -> String.valueOf(n);
        };
    }

    @Override public int getWidth()  { return 160; }
    @Override public int getHeight() { return effects.size() * 10; }
}
