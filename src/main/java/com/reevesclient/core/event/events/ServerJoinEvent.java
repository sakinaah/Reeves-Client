package com.reevesclient.core.event.events;

/** Fired once the player has fully joined a server and the world is ready. */
public record ServerJoinEvent(String serverAddress, boolean isHypixel) {}
