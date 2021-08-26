package net.forthecrown.inventory.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.inventory.builder.options.SimpleCordedOption;
import net.forthecrown.inventory.builder.options.SimpleOption;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class InventoryBuilder implements Builder<BuiltInventory> {

    private final int size;
    private Component title;

    private final Int2ObjectMap<InventoryOption> options = new Int2ObjectOpenHashMap<>();

    private InventoryCloseAction onClose;
    private InventoryAction onOpen;

    public InventoryBuilder(int size, Component title) {
        this(size);
        title(title);
    }

    public InventoryBuilder(int size) {
        this.size = validSize(size);
    }

    private int validSize(int size) {
        Validate.isTrue(isValidSize(size), "Invalid inventory size");
        return size;
    }

    public static boolean isValidSize(int size) {
        return MathUtil.isInRange(size, 9, 54) && size % 9 == 0;
    }

    public int size() {
        return size;
    }

    public Component title() {
        return title;
    }

    public InventoryBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public Int2ObjectMap<InventoryOption> options() {
        return options;
    }

    public InventoryBuilder add(InventoryOption action){
        options.put(action.getSlot(), action);
        return this;
    }

    public InventoryBuilder add(int slot, ItemStack item) {
        return add(slot, item, null);
    }

    public InventoryBuilder add(int column, int row, ItemStack item) {
        return add(column, row, item, null);
    }

    public InventoryBuilder add(int slot, ItemStack item, @Nullable InventoryRunnable runnable){
        return add(new SimpleOption(slot, item, runnable));
    }

    public InventoryBuilder add(int column, int row, ItemStack item, @Nullable InventoryRunnable runnable) {
        return add(new SimpleCordedOption(column, row, item, runnable));
    }

    public InventoryBuilder addAll(InventoryOption... options){
        for (InventoryOption o: options) add(o);
        return this;
    }

    public InventoryBuilder addAll(Iterable<? extends InventoryOption> options){
        for (InventoryOption o: options) add(o);
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
