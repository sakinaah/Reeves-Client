package com.reevesclient.core.hud;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

/**
 * Stores a HUD element's position as normalized screen coordinates [0,1]
 * so it scales correctly with any resolution.
 */
public class HUDPosition {

    private float normalizedX;
    private float normalizedY;
    private float scale;

    public HUDPosition(float normalizedX, float normalizedY, float scale) {
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
        this.scale       = scale;
    }

    /** Returns the actual pixel X based on current window width. */
    public int getPixelX() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return (int) (normalizedX * mc.getWindow().getScaledWidth());
    }

    /** Returns the actual pixel Y based on current window height. */
    public int getPixelY() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return (int) (normalizedY * mc.getWindow().getScaledHeight());
    }

    public float getNormalizedX() { return normalizedX; }
    public float getNormalizedY() { return normalizedY; }
    public float getScale()       { return scale; }

    public void setNormalizedX(float v) { normalizedX = Math.max(0f, Math.min(1f, v)); }
    public void setNormalizedY(float v) { normalizedY = Math.max(0f, Math.min(1f, v)); }
    public void setScale(float v)       { scale       = Math.max(0.25f, Math.min(4f, v)); }

    /** Moves position by screen-space delta (pixels). */
    public void moveByPixels(int dx, int dy) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        if (sw > 0) normalizedX = Math.max(0f, Math.min(1f, normalizedX + (float) dx / sw));
        if (sh > 0) normalizedY = Math.max(0f, Math.min(1f, normalizedY + (float) dy / sh));
    }

    public JsonObject serialize() {
        JsonObject o = new JsonObject();
        o.addProperty("x",     normalizedX);
        o.addProperty("y",     normalizedY);
        o.addProperty("scale", scale);
        return o;
    }

    public static HUDPosition deserialize(JsonObject o) {
        float x     = o.has("x")     ? o.get("x").getAsFloat()     : 0f;
        float y     = o.has("y")     ? o.get("y").getAsFloat()      : 0f;
        float scale = o.has("scale") ? o.get("scale").getAsFloat()  : 1f;
        return new HUDPosition(x, y, scale);
    }
}
