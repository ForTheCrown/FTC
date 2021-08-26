package net.forthecrown.inventory.builder.options;

import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Determines in which order options are loaded.
 * By default, options are set to MID.
 */
public enum OptionPriority {
    LOW ((lows, mids, highs) -> lows),
    MID ((lows, mids, highs) -> mids),
    HIGH ((lows, mids, highs) -> highs);

    private final TierPicker picker;

    OptionPriority(TierPicker picker) {
        this.picker = picker;
    }

    public ObjectList<InventoryOption> pick(ObjectList<InventoryOption> lows, ObjectList<InventoryOption> mids, ObjectList<InventoryOption> highs) {
        return picker.pick(lows, mids, highs);
    }
}

interface TierPicker {
    ObjectList<InventoryOption> pick(ObjectList<InventoryOption> lows, ObjectList<InventoryOption> mids, ObjectList<InventoryOption> highs);
}
