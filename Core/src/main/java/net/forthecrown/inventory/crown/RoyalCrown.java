package net.forthecrown.inventory.crown;

import net.forthecrown.inventory.RoyalItem;
import net.forthecrown.utils.LoreBuilder;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class RoyalCrown extends RoyalItem {
    private byte level;

    public RoyalCrown(ItemStack item) {
        super(item);
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta, CompoundTag nbt) {

    }

    public RoyalCrown(UUID owner, ItemStack item, byte level) {
        super(owner, item);
        this.level = level;
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        this.level = tag.getByte("level");
    }

    @Override
    protected void createLore(LoreBuilder lore) {

    }
}
