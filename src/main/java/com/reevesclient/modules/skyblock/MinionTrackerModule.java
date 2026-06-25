package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

import java.util.*;

/**
 * Tracks placed minion slot usage by reading chat messages about minion placement/removal.
 * Purely informational — the player must place and collect minions themselves.
 */
public class MinionTrackerModule extends Module {

    public record MinionEntry(String type, int tier, String location) {}

    private final List<MinionEntry> minions = new ArrayList<>();
    private int maxSlots = 5; // default; updated via API or chat

    public MinionTrackerModule() {
        super("minion_tracker", "Minion Tracker",
              "Displays a list of placed minions fetched from the Hypixel API.",
              ModuleCategory.SKYBLOCK, false);
    }

    public void setMinions(List<MinionEntry> list) {
        synchronized (minions) {
            minions.clear();
            minions.addAll(list);
        }
    }

    public List<MinionEntry> getMinions() {
        synchronized (minions) { return new ArrayList<>(minions); }
    }

    public int getUsedSlots() { return minions.size(); }
    public int getMaxSlots()  { return maxSlots; }
    public void setMaxSlots(int n) { maxSlots = n; }
}
