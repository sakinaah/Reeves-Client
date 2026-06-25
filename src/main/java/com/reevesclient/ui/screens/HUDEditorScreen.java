package com.reevesclient.ui.screens;

import com.reevesclient.ReevesClient;
import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.hud.HUDManager;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import com.reevesclient.ui.components.RButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Drag-and-drop HUD editor.
 * - Left-click drag: move element.
 * - Scroll wheel: scale element.
 * - Right-click: toggle visibility.
 * - Grid snapping when Shift held.
 */
public class HUDEditorScreen extends Screen {

    private final Screen parent;
    private static final int GRID_SIZE = 8;

    private HUDElement dragging = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private HUDElement selected = null;

    public HUDEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(new RButton(width - 100, height - 30, 90, 22, "Done", b -> {
            ReevesClient.getInstance().getConfigManager().save();
            client.setScreen(parent);
        }));

        addDrawableChild(new RButton(width - 200, height - 30, 90, 22, "Reset All", b -> {
            for (HUDElement el : ReevesClient.getInstance().getHUDManager().getElements()) {
                el.resetPosition();
            }
            ReevesClient.getInstance().getConfigManager().save();
        }));

        addDrawableChild(new RButton(12, height - 30, 110, 22, "Theme", b -> {
            if (selected != null) selected.cycleTheme();
        }));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Render actual game world behind the editor
        // (Screen uses the world renderer; no extra work needed for transparency)

        // Grid overlay
        renderGrid(ctx);

        // Render all HUD elements
        HUDManager hm = ReevesClient.getInstance().getHUDManager();
        for (HUDElement el : hm.getElements()) {
            HUDManager.renderElement(ctx, el, delta);
            renderHandle(ctx, el, mouseX, mouseY);
        }

        // Info bar at bottom
        ctx.fill(0, height - 36, width, height, 0xCC000000);
        RenderUtil.drawText(ctx, "Drag to move  |  Scroll to scale  |  Right-click to toggle  |  Shift = snap to grid",
                8, height - 26, ColorUtil.RC_TEXT_MUTED);

        if (selected != null) {
            String info = selected.getDisplayName() + " — scale: " +
                    String.format("%.2f", selected.getPosition().getScale()) +
                    "  [" + selected.getPosition().getPixelX() + ", " + selected.getPosition().getPixelY() + "]";
            RenderUtil.drawText(ctx, info, 8, height - 36, ColorUtil.RC_TEXT);

            int panelX = width - 250;
            int panelY = 12;
            RenderUtil.fillRoundedRect(ctx, panelX, panelY, 238, 92, 8, ColorUtil.RC_BG_PANEL);
            RenderUtil.drawBorder(ctx, panelX, panelY, 238, 92, 1, ColorUtil.RC_BORDER);
            RenderUtil.drawText(ctx, "Selected HUD", panelX + 10, panelY + 10, ColorUtil.RC_TEXT);
            RenderUtil.drawText(ctx, selected.isShowBackground() ? "Background: ON" : "Background: OFF",
                panelX + 10, panelY + 26, ColorUtil.RC_TEXT_MUTED);
            RenderUtil.drawText(ctx, "Opacity: " + String.format("%.2f", selected.getOpacity()),
                panelX + 10, panelY + 42, ColorUtil.RC_TEXT_MUTED);
                RenderUtil.drawText(ctx, "Theme cycles with left click. Right click toggles background.",
                panelX + 10, panelY + 58, ColorUtil.RC_TEXT_MUTED);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderGrid(DrawContext ctx) {
        for (int x = 0; x < width; x += GRID_SIZE) {
            ctx.fill(x, 0, x + 1, height, 0x11FFFFFF);
        }
        for (int y = 0; y < height; y += GRID_SIZE) {
            ctx.fill(0, y, width, y + 1, 0x11FFFFFF);
        }
    }

    private void renderHandle(DrawContext ctx, HUDElement el, int mouseX, int mouseY) {
        int ex = el.getPosition().getPixelX();
        int ey = el.getPosition().getPixelY();
        int ew = (int) (el.getWidth()  * el.getPosition().getScale());
        int eh = (int) (el.getHeight() * el.getPosition().getScale());

        boolean hovered = mouseX >= ex && mouseX <= ex + ew && mouseY >= ey && mouseY <= ey + eh;
        boolean isSel   = el == selected;

        if (hovered || isSel) {
            RenderUtil.drawBorder(ctx, ex - 1, ey - 1, ew + 2, eh + 2, 1,
                    isSel ? ColorUtil.RC_ACCENT : 0x66FFFFFF);

            if (!el.isVisible()) {
                RenderUtil.drawText(ctx, "[HIDDEN]", ex + 2, ey + 2, ColorUtil.RC_TEXT_MUTED);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean shifted) {
        if (super.mouseClicked(click, shifted)) return true;
        double mx = click.x(); double my = click.y(); int button = click.button();

        HUDElement hit = getElementAt((int) mx, (int) my);
        if (hit != null) {
            if (button == 0) {
                selected    = hit;
                dragging    = hit;
                dragOffsetX = (int) mx - hit.getPosition().getPixelX();
                dragOffsetY = (int) my - hit.getPosition().getPixelY();
            } else if (button == 1) {
                hit.setVisible(!hit.isVisible());
                selected = hit;
            }
            return true;
        }

        if (selected != null) {
            int themeBtnX = 12;
            int themeBtnY = height - 30;
            if (mx >= themeBtnX && mx <= themeBtnX + 110 && my >= themeBtnY && my <= themeBtnY + 22 && button == 0) {
                selected.cycleTheme();
                return true;
            } else if (mx >= themeBtnX && mx <= themeBtnX + 110 && my >= themeBtnY && my <= themeBtnY + 22 && button == 1) {
                selected.setShowBackground(!selected.isShowBackground());
                return true;
            }
        }

        selected = null;
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragging != null) {
            int targetX = (int) click.x() - dragOffsetX;
            int targetY = (int) click.y() - dragOffsetY;

            boolean snap = InputUtil.isKeyPressed(client.getWindow(), InputUtil.GLFW_KEY_LEFT_SHIFT)
                        || InputUtil.isKeyPressed(client.getWindow(), InputUtil.GLFW_KEY_RIGHT_SHIFT);
            if (snap) {
                targetX = Math.round((float) targetX / GRID_SIZE) * GRID_SIZE;
                targetY = Math.round((float) targetY / GRID_SIZE) * GRID_SIZE;
            }

            float nx = (float) Math.max(0, Math.min(width  - dragging.getWidth(),  targetX)) / width;
            float ny = (float) Math.max(0, Math.min(height - dragging.getHeight(), targetY)) / height;
            dragging.getPosition().setNormalizedX(nx);
            dragging.getPosition().setNormalizedY(ny);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        HUDElement hit = getElementAt((int) mx, (int) my);
        if (hit != null) {
            boolean opacityAdjust = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
                    || InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
            if (opacityAdjust) {
                hit.setOpacity(hit.getOpacity() + (float) vScroll * 0.05f);
            } else {
                float newScale = hit.getPosition().getScale() + (float) vScroll * 0.05f;
                hit.getPosition().setScale(newScale);
            }
            selected = hit;
            return true;
        }
        return false;
    }

    private HUDElement getElementAt(int mx, int my) {
        List<HUDElement> elements = ReevesClient.getInstance().getHUDManager().getElements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HUDElement el = elements.get(i);
            int ex = el.getPosition().getPixelX();
            int ey = el.getPosition().getPixelY();
            int ew = (int) (el.getWidth()  * el.getPosition().getScale()) + 4;
            int eh = (int) (el.getHeight() * el.getPosition().getScale()) + 4;
            if (mx >= ex - 2 && mx <= ex + ew && my >= ey - 2 && my <= ey + eh) return el;
        }
        return null;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
