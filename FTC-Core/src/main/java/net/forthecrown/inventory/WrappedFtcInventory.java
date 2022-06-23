package net.forthecrown.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public record WrappedFtcInventory(Inventory inventory) implements FtcInventory {

    public int getSize() {
        return inventory.getSize();
    }

    public int getMaxStackSize() {
        return inventory.getMaxStackSize();
    }

    public void setMaxStackSize(int i) {
        inventory.setMaxStackSize(i);
    }

    public @Nullable
    ItemStack getItem(int i) {
        return inventory.getItem(i);
    }

    public void setItem(int i, @Nullable ItemStack stack) {
        inventory.setItem(i, stack);
    }

    public @NotNull
    HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... stacks) throws IllegalArgumentException {
        return inventory.addItem(stacks);
    }

    public @NotNull
    HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... stacks) throws IllegalArgumentException {
        return inventory.removeItem(stacks);
    }

    public @NotNull
    HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... stacks) throws IllegalArgumentException {
        return inventory.removeItemAnySlot(stacks);
    }

    public @NonNull
    ItemStack[] getContents() {
        return inventory.getContents();
    }

    public void setContents(@NotNull ItemStack[] stacks) throws IllegalArgumentException {
        inventory.setContents(stacks);
    }

    public @NotNull
    ItemStack[] getStorageContents() {
        return inventory.getStorageContents();
    }

    public void setStorageContents(@NotNull ItemStack[] stacks) throws IllegalArgumentException {
        inventory.setStorageContents(stacks);
    }

    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return inventory.contains(material);
    }

    @Contract("null -> false")
    public boolean contains(@Nullable ItemStack stack) {
        return inventory.contains(stack);
    }

    public boolean contains(@NotNull Material material, int i) throws IllegalArgumentException {
        return inventory.contains(material, i);
    }

    @Contract("null, _ -> false")
    public boolean contains(@Nullable ItemStack stack, int i) {
        return inventory.contains(stack, i);
    }

    @Contract("null, _ -> false")
    public boolean containsAtLeast(@Nullable ItemStack stack, int i) {
        return inventory.containsAtLeast(stack, i);
    }

    public @NotNull
    HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        return inventory.all(material);
    }

    public @NotNull
    HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack stack) {
        return inventory.all(stack);
    }

    public int first(@NotNull Material material) throws IllegalArgumentException {
        return inventory.first(material);
    }

    public int first(@NotNull ItemStack stack) {
        return inventory.first(stack);
    }

    public int firstEmpty() {
        return inventory.firstEmpty();
    }

    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public void remove(@NotNull Material material) throws IllegalArgumentException {
        inventory.remove(material);
    }

    public void remove(@NotNull ItemStack stack) {
        inventory.remove(stack);
    }

    public void clear(int i) {
        inventory.clear(i);
    }

    public void clear() {
        inventory.clear();
    }

    public int close() {
        return inventory.close();
    }

    public @NotNull
    List<HumanEntity> getViewers() {
        return inventory.getViewers();
    }

    public @NotNull
    InventoryType getType() {
        return inventory.getType();
    }

    public @Nullable
    InventoryHolder getHolder() {
        return inventory.getHolder();
    }

    public @Nullable
    InventoryHolder getHolder(boolean b) {
        return inventory.getHolder(b);
    }

    public @NotNull
    ListIterator<ItemStack> iterator() {
        return inventory.iterator();
    }

    public @NotNull
    ListIterator<ItemStack> iterator(int i) {
        return inventory.iterator(i);
    }

    public @Nullable
    Location getLocation() {
        return inventory.getLocation();
    }

    public void forEach(Consumer<? super ItemStack> action) {
        inventory.forEach(action);
    }

    public Spliterator<ItemStack> spliterator() {
        return inventory.spliterator();
    }

    @Nullable
    @Override
    public Component title() {
        if (inventory instanceof CraftInventoryCustom custom) {
            return FtcInventory.titleFrom(custom);
        }

        return null;
    }
}
