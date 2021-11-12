package net.forthecrown.inventory;

import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.LoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public abstract class RankedItem extends RoyalItem {
    protected int rank = 1;

    public RankedItem(ItemStack item) {
        super(item);
    }

    public RankedItem(UUID owner, ItemStack item) {
        super(owner, item);
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta, CompoundTag nbt) {
        nbt.putInt("rank", rank);
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        this.rank = tag.getInt("rank");
    }

    @Override
    protected void createLore(LoreBuilder lore) {
        lore.add(
                Component.text("Rank " + FtcUtils.arabicToRoman(rank))
                        .style(nonItalic(NamedTextColor.GRAY))
        );
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
