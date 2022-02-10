package net.forthecrown.economy.selling;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.SoldMaterialData;
import net.forthecrown.user.SellAmount;
import net.forthecrown.inventory.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BlockSellOption implements InventoryOption {

    private final int slot;
    private final Material blockMaterial;
    private final Material ingredientMaterial;
    private final int scalar;

    private BlockSellOption(int slot, Material blockMaterial, Material ingredientMaterial, int scalar) {
        this.slot = slot;
        this.blockMaterial = blockMaterial;
        this.ingredientMaterial = ingredientMaterial;
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

    public Material getIngredientMaterial() {
        return ingredientMaterial;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), makeItem(user.getMatData(getIngredientMaterial()), user.getSellAmount(), getBlockMaterial(), getScalar()));
    }

    public static ItemStack makeItem(SoldMaterialData data, SellAmount sellAmount, Material blockMat, int timer) {
        ItemStackBuilder builder = new ItemStackBuilder(blockMat, sellAmount.getItemAmount());

        ItemSellOption.addLore(data, sellAmount, builder, timer);
        return builder.build();
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(
                SellShops.sell(user, getBlockMaterial(), getScalar(), user.getMatData(getIngredientMaterial())) > 0
        );
    }
}
