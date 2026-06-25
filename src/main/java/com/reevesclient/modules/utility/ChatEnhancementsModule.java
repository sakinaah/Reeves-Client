package com.reevesclient.modules.utility;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.*;
import com.reevesclient.core.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chat enhancements: timestamps, anti-spam filtering, keyword highlighting.
 * Does NOT send anything on behalf of the player or manipulate outgoing packets.
 */
public class ChatEnhancementsModule extends Module {

    private final BooleanSetting showTimestamps;
    private final BooleanSetting filterDuplicates;
    private final StringSetting  keywordHighlight;

    private final List<String> recentMessages = new ArrayList<>();
    private static final int MAX_HISTORY = 50;

    public ChatEnhancementsModule() {
        super("chat_enhancements", "Chat Enhancements",
              "Timestamps, duplicate filtering, and keyword highlighting for chat.",
              ModuleCategory.UTILITY, true);

        showTimestamps   = addSetting(new BooleanSetting(
            "timestamps", "Show Timestamps", "Prepend HH:mm to each message.", true));

        filterDuplicates = addSetting(new BooleanSetting(
            "filter_duplicates", "Filter Duplicates",
            "Hide back-to-back identical messages (common with spam bots).", false));

        keywordHighlight = addSetting(new StringSetting(
            "keyword", "Highlight Keyword",
            "Word to highlight in yellow (case-insensitive). Leave empty to disable.", ""));
    }

    /**
     * Called by MixinChatScreen when the client receives a message.
     * Returns true if the message should be suppressed (duplicate filter).
     */
    public boolean onReceiveMessage(String raw) {
        if (!isEnabled()) return false;

        if (filterDuplicates.getValue()) {
            if (!recentMessages.isEmpty() && recentMessages.get(recentMessages.size() - 1).equals(raw)) {
                return true; // suppress duplicate
            }
        }

        recentMessages.add(raw);
        if (recentMessages.size() > MAX_HISTORY) recentMessages.remove(0);

        return false;
    }

    /** Builds the display prefix (timestamp) if enabled. */
    public String buildPrefix() {
        if (!isEnabled() || !showTimestamps.getValue()) return "";
        return "§8[" + TimeUtil.currentTime24h() + "] §r";
    }

    public String getKeyword() {
        return isEnabled() ? keywordHighlight.getValue() : "";
    }
}
