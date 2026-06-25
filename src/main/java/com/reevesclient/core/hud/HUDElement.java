package com.reevesclient.core.hud;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for all HUD overlays rendered on the in-game screen.
 * Each HUDElement has its own position, scale, visibility, and opacity.
 */
public abstract class HUDElement {

    private final String id;
    private final String displayName;

    protected HUDPosition position;
    private boolean visible  = true;
    private float   opacity  = 1f;
    private boolean showBackground = false;

    protected HUDElement(String id, String displayName, float defaultX, float defaultY) {
        this.id          = id;
        this.displayName = displayName;
        this.position    = new HUDPosition(defaultX, defaultY, 1f);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /** Called on every tick (regardless of visibility). Override for data polling. */
    public void onTick(net.minecraft.client.MinecraftClient client) {}

    /**
     * Called each frame to render this element.
     * drawContext is already positioned; respect {@code position.getScale()}.
     */
    public abstract void render(DrawContext drawContext, float tickDelta);

    /**
     * @return the width of this element in pixels (for editor bounds and snapping).
     */
    public abstract int getWidth();

    /**
     * @return the height of this element in pixels.
     */
    public abstract int getHeight();

    // ── Serialization ────────────────────────────────────────────────────────

    public JsonObject serialize() {
        JsonObject o = new JsonObject();
        o.addProperty("visible",        visible);
        o.addProperty("opacity",        opacity);
        o.addProperty("showBackground", showBackground);
        o.add("position", position.serialize());
        return o;
    }

    public void deserialize(JsonObject o) {
        if (o.has("visible"))        visible        = o.get("visible").getAsBoolean();
        if (o.has("opacity"))        opacity        = o.get("opacity").getAsFloat();
        if (o.has("showBackground")) showBackground = o.get("showBackground").getAsBoolean();
        if (o.has("position"))       position       = HUDPosition.deserialize(o.getAsJsonObject("position"));
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String      getId()             { return id; }
    public String      getDisplayName()    { return displayName; }
    public HUDPosition getPosition()       { return position; }
    public boolean     isVisible()         { return visible; }
    public float       getOpacity()        { return opacity; }
    public boolean     isShowBackground()  { return showBackground; }

    public void setVisible(boolean v)         { visible = v; }
    public void setOpacity(float v)           { opacity = Math.max(0f, Math.min(1f, v)); }
    public void setShowBackground(boolean v)  { showBackground = v; }

    /** Applies alpha of this element to an ARGB color. */
    protected int applyOpacity(int argb) {
        int a = (int) ((argb >> 24 & 0xFF) * opacity);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
