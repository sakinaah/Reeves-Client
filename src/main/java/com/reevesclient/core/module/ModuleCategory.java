package com.reevesclient.core.module;

public enum ModuleCategory {
    HUD          ("HUD",         0xFF5B8DEE),
    PERFORMANCE  ("Performance", 0xFF4CAF50),
    SKYBLOCK     ("SkyBlock",    0xFF00BCD4),
    DUNGEONS     ("Dungeons",    0xFFFF9800),
    PVP          ("PvP",         0xFFE53935),
    UTILITY      ("Utility",     0xFF9C27B0),
    COSMETICS    ("Cosmetics",   0xFFEC407A),
    ACCESSIBILITY("Accessibility",0xFF26A69A),
    STATISTICS   ("Statistics",  0xFF78909C);

    public final String displayName;
    public final int    accentColor;

    ModuleCategory(String displayName, int accentColor) {
        this.displayName = displayName;
        this.accentColor = accentColor;
    }
}
