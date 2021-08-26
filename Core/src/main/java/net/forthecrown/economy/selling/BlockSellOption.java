package net.forthecrown.economy.selling;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BlockSellOption implements InventoryOption {

    private final int slot;
    private final Material blockMat;
    private final Material actualMat;
    private final int scalar;

    private BlockSellOption(int slot, Material blockMat, Material actualMat, int scalar) {
        this.slot = slot;
        this.blockMat = blockMat;
        this.actualMat = actualMat;
        this.scalar = scalar;
    }

    static BlockSellOption nine(int slot, Material blockMat, Material actualMat) {
        return new BlockSellOption(slot, blockMat, actualMat, 9);
    }

    static BlockSellOption four(int slot, Material blockMat, Material actualMat) {
        return new BlockSellOption(slot, blockMat, actualMat, 4);
    }

    public int getScalar() {
        return scalar;
    }

    public Material getActualMat() {
        return actualMat;
    }

    public Material getBlockMat() {
        return blockMat;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), makeItem(user.getMatData(getActualMat()), user.getSellAmount(), getBlockMat(), getScalar()));
    }

    public static ItemStack makeItem(SoldMaterialData data, SellAmount sellAmount, Material blockMat, int timer) {
        ItemStackBuilder builder = new ItemStackBuilder(blockMat, sellAmount.value);

        ItemSellOption.addLore(data, sellAmount, builder, timer);
        return builder.build();
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(
                SellShops.sell(user, getBlockMat(), getScalar(), user.getMatData(getActualMat())) > 0
        );
    }
}
