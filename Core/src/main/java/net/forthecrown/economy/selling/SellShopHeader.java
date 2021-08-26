package net.forthecrown.economy.selling;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryRunnable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * A header or inventory opener for a sellshop section
 */
public class SellShopHeader implements CordedInventoryOption {

    static final SellShopHeader MINERALS = new SellShopHeader(
            (user, context) -> SellShops.MINERALS.open(user),
            Material.AMETHYST_SHARD,
            "Minerals", "Ores and minerals found from the ground."
    );

    static final SellShopHeader MINING = new SellShopHeader(
            (user, context) -> SellShops.MINING.open(user),
            Material.IRON_PICKAXE,
            "Mining", "Blocks attained by mining."
    );

    static final SellShopHeader CRAFTABLE_BLOCKS = new SellShopHeader(
            (user, context) -> SellShops.CRAFTABLE_BLOCKS.open(user),
            Material.IRON_BLOCK,
            "Craftable Blocks", "Blocks made up of smaller parts."
    );

    static final SellShopHeader DROPS = new SellShopHeader(
            (user, context) -> SellShops.DROPS.open(user),
            Material.ROTTEN_FLESH,
            "Mob Drops", "Common items dropped by mobs."
    );

    static final SellShopHeader CROPS = new SellShopHeader(
            (user, context) -> SellShops.CROPS.open(user),
            Material.WHEAT,
            "Farming", "Crops and other farmable items."
    );

    private final InventoryPos slot;
    private final ItemStack item;
    private final InventoryRunnable runnable;

    SellShopHeader(InventoryRunnable runnable, Material material, String name, String... desc) {
        this.slot = new InventoryPos(4, 0);
        this.runnable = runnable;

        ItemStackBuilder builder = new ItemStackBuilder(material, 1)
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setName(Component.text(name).style(FtcFormatter.nonItalic(NamedTextColor.AQUA)));

        for (String s: desc) {
            builder.addLore(Component.text(s).style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        this.item = builder.build();
    }

    SellShopHeader(int column, int row, InventoryRunnable runnable, ItemStack item){
        this.slot = new InventoryPos(column, row);
        this.runnable = runnable;
        this.item = item;
    }

    /**
     * Creates a clone of this header in the given cords
     * @param column The column to create it at
     * @param row The row to create it at
     * @return The header at the given cords.
     */
    SellShopHeader slotClone(int column, int row){
        return new SellShopHeader(column, row, runnable, getItem());
    }

    @Override
    public InventoryPos getPos() {
        return slot;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public InventoryRunnable getRunnable() {
        return runnable;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), getItem());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        runnable.onClick(user, context);
    }
}
