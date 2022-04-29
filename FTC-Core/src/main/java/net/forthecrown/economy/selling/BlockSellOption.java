package net.forthecrown.economy.selling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class BlockSellOption implements InventoryOption {

    @Getter private final int slot;
    @Getter private final Material blockMaterial;
    @Getter private final Material ingredientMaterial;
    @Getter private final int scalar;

    static BlockSellOption nine(int slot, Material blockMat, Material actualMat) {
        return new BlockSellOption(slot, blockMat, actualMat, 9);
    }

    static BlockSellOption four(int slot, Material blockMat, Material actualMat) {
        return new BlockSellOption(slot, blockMat, actualMat, 4);
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