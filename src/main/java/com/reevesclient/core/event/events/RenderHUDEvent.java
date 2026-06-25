package com.reevesclient.core.event.events;

import net.minecraft.client.gui.DrawContext;

public record RenderHUDEvent(DrawContext drawContext, float tickDelta) {}
