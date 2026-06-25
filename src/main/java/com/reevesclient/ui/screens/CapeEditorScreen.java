package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.cape.CapeManager;
import com.reevesclient.cape.CapeProfile;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.RButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Cape editor screen.
 * - Shows a cape preview (2D) with the current cape texture.
 * - Allows uploading a new image (opens system file chooser).
 * - Profile management: create, delete, switch.
 * - Visibility control: self-only or all Reeves Client users.
 */
public class CapeEditorScreen extends Screen {

    private final Screen parent;
    private CapeManager capeManager;

    private int selectedProfileIndex = 0;
    private float previewRotation = 0f;

    public CapeEditorScreen(Screen parent) {
        super(Text.literal("Cape Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        capeManager = ReevesClient.getInstance().getCapeManager();

        int btnX = width / 2 - 100;
        int btnY = height - 36;

        addDrawableChild(new RButton(btnX, btnY, 90, 22, "Upload", b -> openFileChooser()));
        addDrawableChild(new RButton(btnX + 100, btnY, 90, 22, "Remove", b -> removeCurrentCape()));
        addDrawableChild(new RButton(width - 100, btnY, 90, 22, "Back", b -> {
            ReevesClient.getInstance().getConfigManager().save();
            client.setScreen(parent);
        }));

        addDrawableChild(new RButton(10, btnY, 90, 22, "New Profile", b -> createProfile()));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xAA000000);

        int pw = 600, ph = 400;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        RenderUtil.fillRoundedRect(ctx, px, py, pw, ph, 10, ColorUtil.RC_BG);
        RenderUtil.drawBorder(ctx, px, py, pw, ph, 1, ColorUtil.RC_BORDER);

        ctx.fill(px, py, px + pw, py + 36, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "Cape Manager", px + 10, py + 13, ColorUtil.RC_ACCENT);

        // Profile list (left)
        renderProfileList(ctx, px + 10, py + 50, 150, ph - 60);

        // Cape preview (right)
        renderCapePreview(ctx, px + 170, py + 50, pw - 180, ph - 60);

        super.render(ctx, mouseX, mouseY, delta);
        previewRotation += delta * 0.5f;
    }

    private void renderProfileList(DrawContext ctx, int x, int y, int w, int h) {
        RenderUtil.fillRoundedRect(ctx, x, y, w, h, 6, ColorUtil.RC_BG_PANEL);
        RenderUtil.drawText(ctx, "Profiles", x + 8, y + 8, ColorUtil.RC_TEXT_MUTED);

        List<CapeProfile> profiles = capeManager.getProfiles();
        int iy = y + 24;
        for (int i = 0; i < profiles.size(); i++) {
            CapeProfile prof = profiles.get(i);
            boolean sel = i == selectedProfileIndex;
            if (sel) ctx.fill(x, iy, x + w, iy + 22, ColorUtil.RC_SURFACE);
            if (sel) ctx.fill(x, iy, x + 2, iy + 22, ColorUtil.RC_ACCENT);
            RenderUtil.drawText(ctx, prof.getName(), x + 8, iy + 7,
                    sel ? ColorUtil.RC_TEXT : ColorUtil.RC_TEXT_MUTED);
            iy += 24;
        }
    }

    private void renderCapePreview(DrawContext ctx, int x, int y, int w, int h) {
        CapeProfile profile = getSelectedProfile();
        if (profile == null) {
            RenderUtil.drawCenteredText(ctx, "No cape selected.", x + w/2, y + h/2, ColorUtil.RC_TEXT_MUTED);
            return;
        }

        RenderUtil.drawCenteredText(ctx, "Cape: " + profile.getName(), x + w/2, y + 8, ColorUtil.RC_TEXT);

        if (profile.hasTexture()) {
            ctx.drawTexture(RenderPipelines.GUI_TEXTURED, profile.getTextureId(),
                    x + w/2 - 32, y + 30, 0, 0, 64, 32, 64, 32);
        } else {
            RenderUtil.fillRoundedRect(ctx, x + w/2 - 32, y + 30, 64, 32, 4, 0xFF333355);
            RenderUtil.drawCenteredText(ctx, "Upload a cape image", x + w/2, y + 50, ColorUtil.RC_TEXT_MUTED);
        }

        // Info
        int iy = y + 80;
        RenderUtil.drawText(ctx, "Visibility: " + profile.getVisibility().displayName, x + 8, iy, ColorUtil.RC_TEXT);
        iy += 14;
        if (profile.hasTexture()) {
            RenderUtil.drawText(ctx, "Size: " + profile.getTextureSizeString(), x + 8, iy, ColorUtil.RC_TEXT_MUTED);
        }
    }

    private CapeProfile getSelectedProfile() {
        List<CapeProfile> p = capeManager.getProfiles();
        if (p.isEmpty() || selectedProfileIndex >= p.size()) return null;
        return p.get(selectedProfileIndex);
    }

    private void openFileChooser() {
        // Use javax.swing.JFileChooser on a background thread to avoid blocking the render thread
        Thread.ofVirtual().name("reeves-file-chooser").start(() -> {
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                        "Image files", "png", "jpg", "jpeg"));
                int result = fc.showOpenDialog(null);
                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    CapeProfile profile = getSelectedProfile();
                    if (profile != null) {
                        capeManager.loadCapeFromFile(profile, fc.getSelectedFile().toPath());
                    }
                }
            });
        });
    }

    private void removeCurrentCape() {
        CapeProfile profile = getSelectedProfile();
        if (profile != null) capeManager.removeCape(profile);
    }

    private void createProfile() {
        capeManager.createProfile("Cape " + (capeManager.getProfiles().size() + 1));
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        double mx = click.x(); double my = click.y();
        int pw = 600, ph = 400;
        int px = (width - pw) / 2, py = (height - ph) / 2;
        int listX = px + 10, listY = py + 74;
        List<CapeProfile> profiles = capeManager.getProfiles();
        for (int i = 0; i < profiles.size(); i++) {
            int iy = listY + i * 24;
            if (mx >= listX && mx <= listX + 150 && my >= iy && my <= iy + 22) {
                selectedProfileIndex = i;
                capeManager.setActiveProfile(profiles.get(i));
                return true;
            }
        }
        return super.mouseClicked(click, shifted);
    }

    @Override
    public boolean shouldPause() { return false; }
}
