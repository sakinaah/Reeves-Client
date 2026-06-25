package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Displays equipped armor pieces with durability. */
public class ArmorStatusHUD extends HUDElement {

    private final ItemStack[] slots = new ItemStack[4]; // head, chest, legs, feet

    public ArmorStatusHUD() {
        super("armor_status", "Armor Status", 0.88f, 0.55f);
        for (int i = 0; i < 4; i++) slots[i] = ItemStack.EMPTY;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        slots[0] = client.player.getEquippedStack(EquipmentSlot.HEAD);
        slots[1] = client.player.getEquippedStack(EquipmentSlot.CHEST);
        slots[2] = client.player.getEquippedStack(EquipmentSlot.LEGS);
        slots[3] = client.player.getEquippedStack(EquipmentSlot.FEET);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        int y = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = slots[i];
            if (stack == null || stack.isEmpty()) continue;

            // Draw item icon (16x16)
            ctx.drawItem(stack, 0, y);

            // Durability bar
            if (stack.isDamageable()) {
                int maxDmg = stack.getMaxDamage();
                int curDmg = stack.getDamage();
                float pct = 1f - (float) curDmg / maxDmg;
                int barColor = pct > 0.5f ? ColorUtil.RC_SUCCESS
                             : pct > 0.25f ? ColorUtil.RC_WARNING
                             : ColorUtil.RC_ERROR;
                int barW = (int) (pct * 14);
                RenderUtil.fillRect(ctx, 1, y + 14, 14, 1, 0xFF333333);
                if (barW > 0) RenderUtil.fillRect(ctx, 1, y + 14, barW, 1, applyOpacity(barColor));
            }

            // Item name
            String name = stack.getName().getString();
            RenderUtil.drawText(ctx, name, 20, y + 4, applyOpacity(ColorUtil.RC_TEXT));

            y += 18;
        }
    }

    @Override public int getWidth()  { return 130; }
    @Override public int getHeight() { return 4 * 18; }
}
