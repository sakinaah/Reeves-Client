package com.reevesclient;

import com.reevesclient.core.config.ConfigManager;
import com.reevesclient.core.hud.HUDManager;
import com.reevesclient.core.module.ModuleManager;
import com.reevesclient.cape.CapeManager;
import com.reevesclient.api.HypixelAPIClient;
import com.reevesclient.api.SkyBlockAPIClient;
import com.reevesclient.core.event.EventBus;
import com.reevesclient.core.event.events.ServerJoinEvent;
import com.reevesclient.core.event.events.WorldLoadEvent;
import com.reevesclient.modules.dungeons.DungeonScoreModule;
import com.reevesclient.modules.dungeons.SecretWaypointModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ReevesClient implements ClientModInitializer {

    public static final String MOD_ID = "reeves-client";
    public static final String MOD_NAME = "Reeves Client";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    // Global singletons — accessed via ReevesClient.getInstance().*
    private static ReevesClient instance;

    private ConfigManager configManager;
    private ModuleManager moduleManager;
    private HUDManager hudManager;
    private CapeManager capeManager;
    private HypixelAPIClient hypixelAPI;
    private SkyBlockAPIClient skyBlockAPI;

    // Registered key bindings
    public static KeyBinding KEY_OPEN_MENU;
    public static KeyBinding KEY_OPEN_HUD_EDITOR;
    public static KeyBinding KEY_TOGGLE_SPRINT;
    public static KeyBinding KEY_TOGGLE_SNEAK;
    public static KeyBinding KEY_TOGGLE_ITEM_LOCK;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Initializing {} v{}", MOD_NAME, VERSION);

        registerKeyBindings();

        configManager   = new ConfigManager();
        moduleManager   = new ModuleManager();
        hudManager      = new HUDManager();
        capeManager     = new CapeManager();
        hypixelAPI      = new HypixelAPIClient();
        skyBlockAPI     = new SkyBlockAPIClient();

        configManager.load();
        moduleManager.init();
        hudManager.init();
        capeManager.init();

        registerClientEvents();

        LOGGER.info("{} initialized successfully.", MOD_NAME);
    }

    private void registerKeyBindings() {
        KeyBinding.Category rc = KeyBinding.Category.create(Identifier.of(MOD_ID, "general"));

        KEY_OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reeves-client.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                rc
        ));

        KEY_OPEN_HUD_EDITOR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reeves-client.open_hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                rc
        ));

        KEY_TOGGLE_SPRINT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reeves-client.toggle_sprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                rc
        ));

        KEY_TOGGLE_SNEAK = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reeves-client.toggle_sneak",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                rc
        ));

        KEY_TOGGLE_ITEM_LOCK = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.reeves-client.toggle_item_lock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                rc
        ));
    }

    private void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleManager.onTick(client);
            hudManager.onTick(client);
            handleKeyPresses(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            hudManager.render(drawContext, tickCounter.getTickProgress(true));
        });

        // In-world 3D rendering (waypoints now; dungeon secret boxes once map scanning lands).
        com.reevesclient.core.render.WorldRenderer3D.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            EventBus.getInstance().post(new WorldLoadEvent(client.world));
            var serverEntry = client.getCurrentServerEntry();
            if (serverEntry != null) {
                boolean isHypixel = serverEntry.address.toLowerCase().contains("hypixel.net");
                EventBus.getInstance().post(new ServerJoinEvent(serverEntry.address, isHypixel));
            }
            if (moduleManager != null) {
                DungeonScoreModule dsm = moduleManager.get(DungeonScoreModule.class);
                if (dsm != null) dsm.onNewRun();
                SecretWaypointModule swm = moduleManager.get(SecretWaypointModule.class);
                if (swm != null) swm.onNewRun();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            EventBus.getInstance().post(new WorldLoadEvent(null));
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Saving {} config before shutdown.", MOD_NAME);
            configManager.save();
        });
    }

    private void handleKeyPresses(net.minecraft.client.MinecraftClient client) {
        while (KEY_OPEN_MENU.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new com.reevesclient.ui.screens.DashboardScreen(null));
            }
        }

        while (KEY_OPEN_HUD_EDITOR.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new com.reevesclient.ui.screens.HUDEditorScreen(null));
            }
        }
    }

    public static ReevesClient getInstance()  { return instance; }
    public ConfigManager  getConfigManager()  { return configManager; }
    public ModuleManager  getModuleManager()  { return moduleManager; }
    public HUDManager     getHUDManager()     { return hudManager; }
    public CapeManager    getCapeManager()    { return capeManager; }
    public HypixelAPIClient getHypixelAPI()   { return hypixelAPI; }
    public SkyBlockAPIClient getSkyBlockAPI() { return skyBlockAPI; }
}
