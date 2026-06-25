package com.reevesclient.cape;

import com.google.gson.*;
import com.reevesclient.ReevesClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Manages cape profiles, loads cape textures into the Minecraft texture manager,
 * and validates uploaded images.
 *
 * Security measures:
 * - Maximum file size: 512 KB
 * - Maximum dimensions: 512×256
 * - Only PNG/JPEG accepted
 * - Images are normalized to 64×32 format compatible with the cape model
 */
public class CapeManager {

    private static final int MAX_FILE_BYTES = 512 * 1024;
    private static final int CAPE_W = 64, CAPE_H = 32;

    private final List<CapeProfile> profiles = new ArrayList<>();
    private CapeProfile activeProfile = null;

    private final Path capeDir;
    private final Path configFile;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public CapeManager() {
        capeDir    = FabricLoader.getInstance().getConfigDir().resolve("reeves-client/capes");
        configFile = FabricLoader.getInstance().getConfigDir().resolve("reeves-client/cape_config.json");
    }

    public void init() {
        try { Files.createDirectories(capeDir); }
        catch (IOException e) { ReevesClient.LOGGER.warn("Failed to create cape directory: {}", e.getMessage()); }
        load();
    }

    // ── Profile management ───────────────────────────────────────────────────

    public CapeProfile createProfile(String name) {
        CapeProfile p = new CapeProfile(name);
        profiles.add(p);
        save();
        return p;
    }

    public void setActiveProfile(CapeProfile profile) {
        activeProfile = profile;
        save();
    }

    public void removeCape(CapeProfile profile) {
        profile.clearTexture();
        Path tex = capeDir.resolve(profile.getId() + ".png");
        try { Files.deleteIfExists(tex); } catch (IOException ignored) {}
        save();
    }

    public List<CapeProfile> getProfiles()       { return Collections.unmodifiableList(profiles); }
    public CapeProfile       getActiveProfile()  { return activeProfile; }

    // ── File loading ─────────────────────────────────────────────────────────

    /**
     * Loads a cape image from the given path, validates it, normalizes it to 64×32,
     * and registers it as a Minecraft texture.
     */
    public boolean loadCapeFromFile(CapeProfile profile, Path imagePath) {
        try {
            // Size check
            long fileSize = Files.size(imagePath);
            if (fileSize > MAX_FILE_BYTES) {
                ReevesClient.LOGGER.warn("Cape image exceeds 512 KB limit.");
                return false;
            }

            // Load and validate image
            BufferedImage src = ImageIO.read(imagePath.toFile());
            if (src == null) {
                ReevesClient.LOGGER.warn("Failed to read cape image (unsupported format).");
                return false;
            }
            if (src.getWidth() > 512 || src.getHeight() > 256) {
                ReevesClient.LOGGER.warn("Cape image dimensions exceed 512×256.");
                return false;
            }

            // Normalize to 64×32
            BufferedImage normalized = new BufferedImage(CAPE_W, CAPE_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = normalized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, CAPE_W, CAPE_H, null);
            g.dispose();

            // Save normalized texture locally
            Path savePath = capeDir.resolve(profile.getId() + ".png");
            ImageIO.write(normalized, "PNG", savePath.toFile());
            profile.setTexturePath(savePath);
            profile.setTextureSize(normalized.getWidth(), normalized.getHeight());

            // Register texture with Minecraft on the render thread
            MinecraftClient.getInstance().execute(() -> registerTexture(profile, normalized));

            save();
            return true;
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load cape from file: {}", e.getMessage());
            return false;
        }
    }

    private void registerTexture(CapeProfile profile, BufferedImage image) {
        try {
            NativeImage ni = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >>  8) & 0xFF;
                    int b = argb & 0xFF;
                    ni.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            Identifier id = Identifier.of(ReevesClient.MOD_ID, "cape/" + profile.getId());
            MinecraftClient.getInstance().getTextureManager()
                    .registerTexture(id, new NativeImageBackedTexture(() -> id.toString(), ni));
            profile.setTextureId(id);
            ReevesClient.LOGGER.info("Cape texture registered: {}", id);
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to register cape texture: {}", e.getMessage());
        }
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    private void save() {
        try {
            Files.createDirectories(configFile.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("active", activeProfile != null ? activeProfile.getId() : "");
            JsonArray arr = new JsonArray();
            for (CapeProfile p : profiles) {
                JsonObject o = new JsonObject();
                o.addProperty("id",         p.getId());
                o.addProperty("name",       p.getName());
                o.addProperty("visibility", p.getVisibility().name());
                o.addProperty("animated",   p.isAnimated());
                o.addProperty("frameCount", p.getFrameCount());
                o.addProperty("frameDelay", p.getFrameDelayMs());
                arr.add(o);
            }
            root.add("profiles", arr);
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(configFile), StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (IOException e) {
            ReevesClient.LOGGER.warn("Failed to save cape config: {}", e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(configFile)) return;
        try (Reader r = new InputStreamReader(Files.newInputStream(configFile), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            String activeId = root.has("active") ? root.get("active").getAsString() : "";
            if (root.has("profiles")) {
                for (JsonElement el : root.getAsJsonArray("profiles")) {
                    JsonObject o = el.getAsJsonObject();
                    CapeProfile p = new CapeProfile(o.get("name").getAsString());
                    if (o.has("visibility")) {
                        try { p.setVisibility(CapeProfile.Visibility.valueOf(o.get("visibility").getAsString())); }
                        catch (IllegalArgumentException ignored) {}
                    }
                    profiles.add(p);
                    // Reload texture from saved file
                    Path tex = capeDir.resolve(p.getId() + ".png");
                    if (Files.exists(tex)) {
                        MinecraftClient.getInstance().execute(() -> {
                            try {
                                BufferedImage img = ImageIO.read(tex.toFile());
                                if (img != null) registerTexture(p, img);
                            } catch (IOException e2) {
                                ReevesClient.LOGGER.warn("Failed to reload cape: {}", e2.getMessage());
                            }
                        });
                    }
                    if (p.getId().equals(activeId)) activeProfile = p;
                }
            }
        } catch (Exception e) {
            ReevesClient.LOGGER.warn("Failed to load cape config: {}", e.getMessage());
        }
    }
}
