package com.reevesclient.core.util;

/** ARGB color helpers used throughout the client. */
public final class ColorUtil {

    private ColorUtil() {}

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b)   { return argb(255, r, g, b); }
    public static int withAlpha(int rgb, int a)  { return (rgb & 0x00FFFFFF) | ((a & 0xFF) << 24); }
    public static int alpha(int argb)            { return (argb >> 24) & 0xFF; }
    public static int red(int argb)              { return (argb >> 16) & 0xFF; }
    public static int green(int argb)            { return (argb >> 8)  & 0xFF; }
    public static int blue(int argb)             { return  argb        & 0xFF; }

    /** Linearly interpolates between two ARGB colors. t ∈ [0,1]. */
    public static int lerp(int a, int b, float t) {
        int ar = red(a),   ag = green(a), ab = blue(a), aa = alpha(a);
        int br = red(b),   bg = green(b), bb = blue(b), ba = alpha(b);
        return argb(
            (int) (aa + (ba - aa) * t),
            (int) (ar + (br - ar) * t),
            (int) (ag + (bg - ag) * t),
            (int) (ab + (bb - ab) * t)
        );
    }

    /** Converts HSV (0–360, 0–1, 0–1) to ARGB (alpha=255). */
    public static int hsvToArgb(float h, float s, float v) {
        if (s == 0f) {
            int c = (int) (v * 255);
            return rgb(c, c, c);
        }
        h = h % 360f;
        float sec = h / 60f;
        int   i   = (int) sec;
        float f   = sec - i;
        float p   = v * (1 - s);
        float q   = v * (1 - s * f);
        float t2  = v * (1 - s * (1 - f));
        float r, g, b;
        switch (i) {
            case 0  -> { r = v;  g = t2; b = p;  }
            case 1  -> { r = q;  g = v;  b = p;  }
            case 2  -> { r = p;  g = v;  b = t2; }
            case 3  -> { r = p;  g = q;  b = v;  }
            case 4  -> { r = t2; g = p;  b = v;  }
            default -> { r = v;  g = p;  b = q;  }
        }
        return rgb((int)(r*255), (int)(g*255), (int)(b*255));
    }

    // ── Reeves Client design system colors ───────────────────────────────────
    // NOT final: the ThemeManager mutates these at runtime so the whole UI can be
    // re-themed live. (final primitives would be inlined at compile time and could
    // never change.)
    public static int RC_ACCENT        = 0xFF5B8DEE; // blue
    public static int RC_BG            = 0xDD1A1A2E; // dark navy
    public static int RC_BG_PANEL      = 0xDD16213E; // panel bg
    public static int RC_SURFACE       = 0xDD0F3460; // surface
    public static int RC_TEXT          = 0xFFE0E0E0; // primary text
    public static int RC_TEXT_MUTED    = 0xFF9E9E9E; // muted text
    public static int RC_BORDER        = 0x33FFFFFF; // subtle border
    public static int RC_SUCCESS       = 0xFF4CAF50;
    public static int RC_WARNING       = 0xFFFF9800;
    public static int RC_ERROR         = 0xFFE53935;

    /** Global text-shadow toggle, honoured by {@code RenderUtil.drawText}. */
    public static boolean TEXT_SHADOW = true;
}
