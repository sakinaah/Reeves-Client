package com.reevesclient.core.theme;

import com.reevesclient.core.config.ConfigManager;
import com.reevesclient.core.util.ColorUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unified appearance/theme system. Holds the editable palette + panel opacity +
 * text-shadow flag, persists them, and pushes them into {@link ColorUtil} so the
 * entire Reeves UI (and every HUD that uses the design-system colors) re-themes
 * at once. No third-party code; purely Reeves UI state.
 */
public class ThemeManager {

    // Built-in defaults (the original design-system palette).
    private static final int D_ACCENT  = 0xFF5B8DEE;
    private static final int D_BG      = 0x1A1A2E;
    private static final int D_PANEL   = 0x16213E;
    private static final int D_SURFACE = 0x0F3460;
    private static final int D_TEXT    = 0xFFE0E0E0;
    private static final int D_MUTED   = 0xFF9E9E9E;
    private static final int D_BORDER  = 0x33FFFFFF;

    // Editable state (bg/panel/surface stored as 0xRRGGBB; alpha applied separately).
    private int     accent  = D_ACCENT;
    private int     bg      = D_BG;
    private int     panel   = D_PANEL;
    private int     surface = D_SURFACE;
    private int     text    = D_TEXT;
    private int     muted   = D_MUTED;
    private int     border  = D_BORDER;
    private int     bgAlpha = 0xDD;
    private boolean textShadow = true;

    /** A named palette the user can apply in one click. */
    public record Preset(int accent, int bg, int panel, int surface, int text, int muted) {}

    private static final Map<String, Preset> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("Midnight",  new Preset(0xFF5B8DEE, 0x1A1A2E, 0x16213E, 0x0F3460, 0xFFE0E0E0, 0xFF9E9E9E));
        PRESETS.put("Crimson",   new Preset(0xFFE53935, 0x1E1414, 0x2A1A1A, 0x3A1F1F, 0xFFF0E0E0, 0xFFB89090));
        PRESETS.put("Emerald",   new Preset(0xFF2ECC71, 0x121E16, 0x16291E, 0x1B3A29, 0xFFE0F0E6, 0xFF93B8A4));
        PRESETS.put("Amethyst",  new Preset(0xFFB061FF, 0x1A1424, 0x241A33, 0x2F2147, 0xFFEDE0FF, 0xFFB1A0C3));
        PRESETS.put("Aqua",      new Preset(0xFF00BCD4, 0x10202A, 0x142A36, 0x1B3A4A, 0xFFE0F2F7, 0xFF8FB3C0));
        PRESETS.put("Light",     new Preset(0xFF2962FF, 0xE8E8EE, 0xF2F2F7, 0xDCDCE6, 0xFF1B1B22, 0xFF5A5A66));
    }

    public static Map<String, Preset> presets() { return PRESETS; }

    public void applyPreset(String name) {
        Preset p = PRESETS.get(name);
        if (p == null) return;
        accent = p.accent(); bg = p.bg(); panel = p.panel(); surface = p.surface();
        text = p.text(); muted = p.muted();
        apply();
    }

    /** Pushes the current theme into ColorUtil so the whole UI updates. */
    public void apply() {
        ColorUtil.RC_ACCENT     = accent;
        ColorUtil.RC_TEXT       = text;
        ColorUtil.RC_TEXT_MUTED = muted;
        ColorUtil.RC_BORDER     = border;
        ColorUtil.RC_BG         = ColorUtil.withAlpha(bg,      bgAlpha);
        ColorUtil.RC_BG_PANEL   = ColorUtil.withAlpha(panel,   bgAlpha);
        ColorUtil.RC_SURFACE    = ColorUtil.withAlpha(surface, bgAlpha);
        ColorUtil.TEXT_SHADOW   = textShadow;
    }

    public void load(ConfigManager c) {
        accent     = c.getInt("theme.accent",  D_ACCENT);
        bg         = c.getInt("theme.bg",      D_BG);
        panel      = c.getInt("theme.panel",   D_PANEL);
        surface    = c.getInt("theme.surface", D_SURFACE);
        text       = c.getInt("theme.text",    D_TEXT);
        muted      = c.getInt("theme.muted",   D_MUTED);
        border     = c.getInt("theme.border",  D_BORDER);
        bgAlpha    = c.getInt("theme.bgAlpha", 0xDD);
        textShadow = c.getBoolean("theme.textShadow", true);
        apply();
    }

    public void save(ConfigManager c) {
        c.setInt("theme.accent",  accent);
        c.setInt("theme.bg",      bg);
        c.setInt("theme.panel",   panel);
        c.setInt("theme.surface", surface);
        c.setInt("theme.text",    text);
        c.setInt("theme.muted",   muted);
        c.setInt("theme.border",  border);
        c.setInt("theme.bgAlpha", bgAlpha);
        c.setBoolean("theme.textShadow", textShadow);
        c.save();
    }

    public void resetDefaults() {
        accent = D_ACCENT; bg = D_BG; panel = D_PANEL; surface = D_SURFACE;
        text = D_TEXT; muted = D_MUTED; border = D_BORDER; bgAlpha = 0xDD; textShadow = true;
        apply();
    }

    // ── Accessors ────────────────────────────────────────────────────────────
    public int     getAccent()     { return accent; }
    public int     getBgAlpha()    { return bgAlpha; }
    public boolean isTextShadow()  { return textShadow; }

    public void setAccent(int argb)      { this.accent = 0xFF000000 | (argb & 0xFFFFFF); apply(); }
    public void setBgAlpha(int a)        { this.bgAlpha = Math.max(0, Math.min(255, a)); apply(); }
    public void setTextShadow(boolean v) { this.textShadow = v; apply(); }
}
