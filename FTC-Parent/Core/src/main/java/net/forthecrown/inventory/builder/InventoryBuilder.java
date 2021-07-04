package net.forthecrown.inventory.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.inventory.ItemStack;

public class InventoryBuilder implements Builder<BuiltInventory> {

    private final int size;
    private final Component title;

    private final Int2ObjectMap<InventoryOption> options = new Int2ObjectOpenHashMap<>();

    private InventoryCloseAction onClose;
    private InventoryAction onOpen;

    public InventoryBuilder(int size, Component title) {
        this.size = size;
        this.title = title;
    }

    public int size() {
        return size;
    }

    public Component title() {
        return title;
    }

    public Int2ObjectMap<InventoryOption> options() {
        return options;
    }

    public InventoryBuilder addOption(InventoryOption action){
        options.put(action.getSlot(), action);
        return this;
    }

    public InventoryBuilder addOption(int slot, ItemStack item, InventoryRunnable runnable){
        return addOption(new SimpleOption(slot, item, runnable));
    }

    public InventoryBuilder addOptions(InventoryOption... options){
        for (InventoryOption o: options) addOption(o);
        return this;
    }

    public InventoryBuilder addOptions(Iterable<InventoryOption> options){
        for (InventoryOption o: options) addOption(o);
        return this;
    }

    public InventoryCloseAction onClose() {
        return onClose;
    }

    public InventoryBuilder onClose(InventoryCloseAction onClose) {
        this.onClose = onClose;
        return this;
    }

    public InventoryAction onOpen() {
        return onOpen;
    }

    public InventoryBuilder onOpen(InventoryAction onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    @Override
    public BuiltInventory build() {
        return new BuiltInventory(
                options,
                title, size,
                onClose, onOpen
        );
    }
}
