package com.reevesclient.core.event.events;

import net.minecraft.text.Text;

/** Fired when the client receives a chat message. Cancel to suppress display. */
public class ChatReceiveEvent {
    private Text message;
    private boolean cancelled;

    public ChatReceiveEvent(Text message) {
        this.message = message;
    }

    public Text getMessage()          { return message; }
    public void setMessage(Text msg)  { this.message = msg; }
    public boolean isCancelled()      { return cancelled; }
    public void setCancelled(boolean v) { this.cancelled = v; }
}
