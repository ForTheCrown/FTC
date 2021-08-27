package net.forthecrown.inventory.builder.options;

import java.util.List;

/**
 * Determines in which order options are loaded.
 * By default, options are set to MID.
 */
public enum OptionPriority {
    LOW {
        @Override
        public List<InventoryOption> pick(List<InventoryOption> lows, List<InventoryOption> mids, List<InventoryOption> highs) {
            return lows;
        }
    },

    MID {
        @Override
        public List<InventoryOption> pick(List<InventoryOption> lows, List<InventoryOption> mids, List<InventoryOption> highs) {
            return mids;
        }
    },

    HIGH {
        @Override
        public List<InventoryOption> pick(List<InventoryOption> lows, List<InventoryOption> mids, List<InventoryOption> highs) {
            return highs;
        }
    };

    public abstract List<InventoryOption> pick(List<InventoryOption> lows, List<InventoryOption> mids, List<InventoryOption> highs);
}
