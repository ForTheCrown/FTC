package net.forthecrown.inventory.crown;

import net.forthecrown.inventory.RoyalItem;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RoyalCrown extends RoyalItem {
    private byte level;

    public RoyalCrown(ItemStack item) {
        super(item);
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
    protected void writeNBT(CompoundTag tag) {
        tag.putByte("level", level);
    }

    @Override
    protected Component renderLore() {
        return null;
    }
}
