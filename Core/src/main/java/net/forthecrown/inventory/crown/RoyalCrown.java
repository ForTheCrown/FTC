package net.forthecrown.inventory.crown;

import net.forthecrown.inventory.RankedItem;
import net.forthecrown.utils.LoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public class RoyalCrown extends RankedItem {

    private CrownRank waitingApplication;
    private boolean queen;

    public RoyalCrown(ItemStack item) {
        super(item, Crowns.TAG_KEY);

        load();
    }

    public RoyalCrown(UUID owner, ItemStack item ) {
        super(owner, item, Crowns.TAG_KEY);
    }

    public void upgrade() {
        rank++;
        waitingApplication = Crowns.getRank(rank);

        update();
    }

    public boolean isQueen() {
        return queen;
    }

    public void setQueen(boolean queen) {
        this.queen = queen;
    }

    @Override
    protected void onUpdate(ItemStack item, ItemMeta meta, CompoundTag nbt) {
        super.onUpdate(item, meta, nbt);
        nbt.putBoolean("queen", queen);

        if(waitingApplication != null) {
            waitingApplication.apply(item, this, meta);
            waitingApplication = null;
        }
    }

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);

        this.queen = tag.getBoolean("queen");
    }

    @Override
    protected void createLore(LoreBuilder lore) {
        super.createLore(lore);

        Component border = Component.text("--------------------------------").style(nonItalic(NamedTextColor.DARK_GRAY));

        lore
                .add(border)
                .add(
                        Component.text("Only the worthy shall wear this").style(nonItalic(NamedTextColor.GOLD))
                )
                .add(
                        Component.text("symbol of strength and power.").style(nonItalic(NamedTextColor.GOLD))
                )
                .add(border);

        if(hasPlayerOwner()) {
            lore.add(
                    Component.text("Crafted for ")
                            .style(nonItalic(NamedTextColor.DARK_GRAY))
                            .append(Component.text((queen ? "Queen" : "King") + ' '))
                            .append(getOwnerUser().nickDisplayName())
            );
        }
    }
}
