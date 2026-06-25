package com.reevesclient.modules.accessibility;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import com.reevesclient.core.module.settings.ModuleSetting.BooleanSetting;

/**
 * Client-side protection for accidental item drops.
 * Each hotbar slot can be locked independently.
 */
public class HotbarLockModule extends Module {

    private static final int HOTBAR_SIZE = 9;

    // NOTE: do NOT use a field initializer here. registerSettings() is invoked from
    // the Module constructor (super(...)), which runs *before* subclass field
    // initializers — an initializer would null this array out again and the
    // settings registered during construction would be lost. Allocate inside
    // registerSettings() instead.
    private BooleanSetting[] lockedSlots;

    public HotbarLockModule() {
        super("hotbar_lock", "Hotbar Lock",
                "Prevents accidental dropping from selected hotbar slots.",
                ModuleCategory.ACCESSIBILITY, false);
    }

    @Override
    protected void registerSettings() {
        lockedSlots = new BooleanSetting[HOTBAR_SIZE];
        for (int slot = 0; slot < lockedSlots.length; slot++) {
            lockedSlots[slot] = addSetting(new BooleanSetting(
                    "slot_" + slot,
                    "Lock Slot " + (slot + 1),
                    "Blocks dropping items from hotbar slot " + (slot + 1) + ".",
                    false
            ));
        }
    }

    public boolean isSlotLocked(int slot) {
        return lockedSlots != null
                && slot >= 0 && slot < lockedSlots.length
                && lockedSlots[slot].getValue();
    }
}