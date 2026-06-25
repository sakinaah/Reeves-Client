package com.reevesclient.modules.dungeons;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.util.HypixelUtil;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;

/**
 * Decodes the dungeon map item (a vanilla filled map Hypixel keeps in the
 * player's inventory) into a texture the {@link com.reevesclient.modules.hud.DungeonMapHUD}
 * can draw. Reading a map the server already sends us is purely informational and
 * Hypixel-compliant — nothing is automated.
 *
 * The texture is rebuilt at most once per second to keep render cost negligible.
 */
public class DungeonMapModule extends Module {

    private static final int MAP_DIM = 128;
    private static final int REBUILD_INTERVAL_TICKS = 20;
    private static final Identifier MAP_TEXTURE_ID = Identifier.of(ReevesClient.MOD_ID, "dungeon_map");

    private boolean hasMap = false;
    private int     tickCounter = 0;

    public DungeonMapModule() {
        super("dungeon_map", "Dungeon Map",
                "Renders the dungeon map overlay from the in-game map item. Informational only.",
                ModuleCategory.DUNGEONS, true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || client.player == null || client.world == null || !HypixelUtil.isInDungeon()) {
            hasMap = false;
            return;
        }
        if (tickCounter++ % REBUILD_INTERVAL_TICKS != 0) return;

        try {
            MapState state = findDungeonMap(client);
            if (state == null) { hasMap = false; return; }
            rebuildTexture(client, state);
        } catch (Exception e) {
            ReevesClient.LOGGER.debug("Dungeon map rebuild skipped: {}", e.getMessage());
            hasMap = false;
        }
    }

    private MapState findDungeonMap(MinecraftClient client) {
        PlayerInventory inv = client.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(Items.FILLED_MAP)) {
                MapState st = FilledMapItem.getMapState(s, client.world);
                if (st != null) return st;
            }
        }
        return null;
    }

    private void rebuildTexture(MinecraftClient client, MapState state) {
        byte[] colors = state.colors;
        if (colors == null || colors.length < MAP_DIM * MAP_DIM) { hasMap = false; return; }

        NativeImage img = new NativeImage(MAP_DIM, MAP_DIM, false);
        for (int i = 0; i < MAP_DIM * MAP_DIM; i++) {
            int argb = MapColor.getRenderColor(colors[i] & 0xFF);
            int a = (argb >>> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8)  & 0xFF;
            int b =  argb        & 0xFF;
            // NativeImage stores ABGR.
            img.setColor(i % MAP_DIM, i / MAP_DIM, (a << 24) | (b << 16) | (g << 8) | r);
        }
        client.getTextureManager().registerTexture(MAP_TEXTURE_ID,
                new NativeImageBackedTexture(MAP_TEXTURE_ID::toString, img));
        hasMap = true;
    }

    public boolean    hasMap()       { return hasMap; }
    public Identifier getTextureId() { return MAP_TEXTURE_ID; }
    public int        getMapDim()    { return MAP_DIM; }
}
