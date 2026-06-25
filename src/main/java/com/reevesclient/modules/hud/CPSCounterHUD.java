package com.reevesclient.modules.hud;

import com.reevesclient.core.hud.HUDElement;
import com.reevesclient.core.util.ColorUtil;
import com.reevesclient.core.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Counts left-click and right-click events per second. */
public class CPSCounterHUD extends HUDElement {

    private final Deque<Long> leftClicks  = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    private boolean leftWasDown  = false;
    private boolean rightWasDown = false;

    public CPSCounterHUD() {
        super("cps_counter", "CPS Counter", 0.01f, 0.05f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        long windowStart = now - 1000;

        // Poll button states (only register press-transitions, not holds)
        long windowHandle = client.getWindow().getHandle();
        boolean leftNow  = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT)  == GLFW.GLFW_PRESS;
        boolean rightNow = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (leftNow  && !leftWasDown)  leftClicks.addLast(now);
        if (rightNow && !rightWasDown) rightClicks.addLast(now);

        leftWasDown  = leftNow;
        rightWasDown = rightNow;

        leftClicks.removeIf(t  -> t < windowStart);
        rightClicks.removeIf(t -> t < windowStart);
    }

    @Override
    public void render(DrawContext ctx, float tickDelta) {
        String text = "L: " + leftClicks.size() + "  R: " + rightClicks.size() + " CPS";
        RenderUtil.drawText(ctx, text, 0, 0, themedText(ColorUtil.RC_TEXT));
    }

    @Override public int getWidth()  { return 100; }
    @Override public int getHeight() { return 9; }
}
