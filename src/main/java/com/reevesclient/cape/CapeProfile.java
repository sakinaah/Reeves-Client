package com.reevesclient.cape;

import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.UUID;

/** Represents a single saved cape configuration. */
public class CapeProfile {

    public enum Visibility {
        SELF_ONLY("Self Only"),
        REEVES_USERS("Reeves Client Users"),
        EVERYONE("Everyone");

        public final String displayName;
        Visibility(String displayName) { this.displayName = displayName; }
    }

    private final String id;
    private String name;
    private Path   texturePath;
    private Identifier textureId; // registered once loaded
    private Visibility visibility = Visibility.REEVES_USERS;
    private boolean animated     = false;
    private int     frameCount   = 1;
    private int     frameDelayMs = 100;
    private int     textureSizeW = 0;
    private int     textureSizeH = 0;

    public CapeProfile(String name) {
        this.id   = UUID.randomUUID().toString();
        this.name = name;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String     getId()              { return id; }
    public String     getName()            { return name; }
    public Path       getTexturePath()     { return texturePath; }
    public Identifier getTextureId()       { return textureId; }
    public Visibility getVisibility()      { return visibility; }
    public boolean    isAnimated()         { return animated; }
    public int        getFrameCount()      { return frameCount; }
    public int        getFrameDelayMs()    { return frameDelayMs; }
    public boolean    hasTexture()         { return textureId != null; }
    public String     getTextureSizeString() {
        return hasTexture() ? textureSizeW + "×" + textureSizeH : "—";
    }

    public void setName(String name)              { this.name = name; }
    public void setTexturePath(Path path)         { this.texturePath = path; }
    public void setTextureId(Identifier id)       { this.textureId = id; }
    public void setVisibility(Visibility v)       { this.visibility = v; }
    public void setAnimated(boolean a)            { this.animated = a; }
    public void setFrameCount(int n)              { this.frameCount = Math.max(1, n); }
    public void setFrameDelay(int ms)             { this.frameDelayMs = Math.max(50, ms); }
    public void setTextureSize(int w, int h)      { this.textureSizeW = w; this.textureSizeH = h; }

    public void clearTexture() {
        textureId   = null;
        texturePath = null;
        animated    = false;
        frameCount  = 1;
    }
}
