package net.forthecrown.inventory;

import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public abstract class RankedItem extends RoyalItem {
    protected int rank = 1;

    public RankedItem(ItemStack item, String tagKey) {
        super(item, tagKey);
    }

    public RankedItem(UUID owner, ItemStack item, String tagKey) {
        super(owner, item, tagKey);
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
    protected void createLore(ItemLoreBuilder lore) {
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
