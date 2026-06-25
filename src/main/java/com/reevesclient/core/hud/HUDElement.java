package com.reevesclient.core.hud;

import com.google.gson.JsonObject;
import com.reevesclient.core.util.ColorUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for all HUD overlays rendered on the in-game screen.
 * Each HUDElement has its own position, scale, visibility, and opacity.
 */
public abstract class HUDElement {

    private final String id;
    private final String displayName;

    protected HUDPosition position;
    private final float defaultX;
    private final float defaultY;
    private boolean visible  = true;
    private float   opacity  = 1f;
    private boolean showBackground = false;
    private int textColor = ColorUtil.RC_TEXT;
    private int mutedTextColor = ColorUtil.RC_TEXT_MUTED;
    private int accentColor = ColorUtil.RC_ACCENT;
    private int backgroundColor = ColorUtil.RC_BG_PANEL;

    protected HUDElement(String id, String displayName, float defaultX, float defaultY) {
        this.id          = id;
        this.displayName = displayName;
        this.defaultX    = defaultX;
        this.defaultY    = defaultY;
        this.position    = new HUDPosition(defaultX, defaultY, 1f);
    }

    /** Restores this element's position, scale and opacity to its defaults. */
    public void resetPosition() {
        position.setNormalizedX(defaultX);
        position.setNormalizedY(defaultY);
        position.setScale(1f);
        setOpacity(1f);
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
        o.addProperty("textColor",      textColor);
        o.addProperty("mutedTextColor",  mutedTextColor);
        o.addProperty("accentColor",     accentColor);
        o.addProperty("backgroundColor", backgroundColor);
        o.add("position", position.serialize());
        return o;
    }

    public void deserialize(JsonObject o) {
        if (o.has("visible"))        visible        = o.get("visible").getAsBoolean();
        if (o.has("opacity"))        opacity        = o.get("opacity").getAsFloat();
        if (o.has("showBackground")) showBackground = o.get("showBackground").getAsBoolean();
        if (o.has("textColor"))      textColor      = o.get("textColor").getAsInt();
        if (o.has("mutedTextColor")) mutedTextColor = o.get("mutedTextColor").getAsInt();
        if (o.has("accentColor"))    accentColor    = o.get("accentColor").getAsInt();
        if (o.has("backgroundColor")) backgroundColor = o.get("backgroundColor").getAsInt();
        if (o.has("position"))       position       = HUDPosition.deserialize(o.getAsJsonObject("position"));
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String      getId()             { return id; }
    public String      getDisplayName()    { return displayName; }
    public HUDPosition getPosition()       { return position; }
    public boolean     isVisible()         { return visible; }
    public float       getOpacity()        { return opacity; }
    public boolean     isShowBackground()  { return showBackground; }
    public int         getTextColor()      { return textColor; }
    public int         getMutedTextColor() { return mutedTextColor; }
    public int         getAccentColor()    { return accentColor; }
    public int         getBackgroundColor(){ return backgroundColor; }

    public void setVisible(boolean v)         { visible = v; }
    public void setOpacity(float v)           { opacity = Math.max(0f, Math.min(1f, v)); }
    public void setShowBackground(boolean v)  { showBackground = v; }
    public void setTextColor(int color)       { textColor = color; }
    public void setMutedTextColor(int color)  { mutedTextColor = color; }
    public void setAccentColor(int color)     { accentColor = color; }
    public void setBackgroundColor(int color) { backgroundColor = color; }

    public void cycleTheme() {
        int[][] themes = new int[][] {
                { ColorUtil.RC_TEXT, ColorUtil.RC_TEXT_MUTED, ColorUtil.RC_ACCENT, ColorUtil.RC_BG_PANEL },
                { 0xFFE6E0D6, 0xFFB8AFA1, 0xFF57C7FF, 0xFF1B232B },
                { 0xFFF4F1EA, 0xFFB9B1A5, 0xFFFFB347, 0xFF2A1E14 },
                { 0xFFEAF4FF, 0xFF90A9C3, 0xFF7DFFB1, 0xFF121A24 }
        };

        for (int i = 0; i < themes.length; i++) {
            int[] theme = themes[i];
            if (textColor == theme[0] && mutedTextColor == theme[1]
                    && accentColor == theme[2] && backgroundColor == theme[3]) {
                int[] next = themes[(i + 1) % themes.length];
                textColor = next[0];
                mutedTextColor = next[1];
                accentColor = next[2];
                backgroundColor = next[3];
                return;
            }
        }

        int[] next = themes[1];
        textColor = next[0];
        mutedTextColor = next[1];
        accentColor = next[2];
        backgroundColor = next[3];
    }

    protected int themedText(int fallback) { return applyOpacity(textColor != 0 ? textColor : fallback); }
    protected int themedMutedText(int fallback) { return applyOpacity(mutedTextColor != 0 ? mutedTextColor : fallback); }
    protected int themedAccent(int fallback) { return applyOpacity(accentColor != 0 ? accentColor : fallback); }
    protected int themedBackground(int fallback) { return applyOpacity(backgroundColor != 0 ? backgroundColor : fallback); }

    /** Applies alpha of this element to an ARGB color. */
    protected int applyOpacity(int argb) {
        int a = (int) ((argb >> 24 & 0xFF) * opacity);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
