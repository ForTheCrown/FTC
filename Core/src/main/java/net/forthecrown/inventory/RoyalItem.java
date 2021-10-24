package net.forthecrown.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public abstract class RoyalItem {
    private final UUID owner;
    private final ItemStack item;

    public RoyalItem(ItemStack item) {
        this.item = item;

        CompoundTag tag = CraftItemStack.asNMSCopy(item).getTagElement("royal_item");
        assert tag != null : "Tag is null";

        this.owner = tag.getUUID("owner");

        readNBT(tag);
    }

    public RoyalItem(UUID owner, ItemStack item) {
        this.owner = owner;
        this.item = item;
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack update() {
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);

        CompoundTag tag = nms.getOrCreateTagElement("royal_item");
        writeNBT(tag);
        nms.addTagElement("royal_item", tag);

        ItemStack result = CraftItemStack.asBukkitCopy(nms);
        ItemMeta meta = result.getItemMeta();
        List<Component> lore = meta.hasLore() ? meta.lore() : new ObjectArrayList<>();
        int loreIndex = lore.isEmpty() ? 0 : lore.size() - 1;

        lore.set(loreIndex, renderLore());
        meta.lore(lore);
        result.setItemMeta(meta);

        return result;
    }

    protected abstract void readNBT(CompoundTag tag);
    protected abstract void writeNBT(CompoundTag tag);
    protected abstract Component renderLore();
}
