package com.reevesclient.core.event.events;

import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

/** Fired when the client loads or unloads a world. world is null on disconnect. */
public record WorldLoadEvent(@Nullable ClientWorld world) {
    public boolean isDisconnect() { return world == null; }
}
