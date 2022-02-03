package net.forthecrown.economy.selling;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.SoldMaterialData;
import net.forthecrown.user.SellAmount;
import net.forthecrown.inventory.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSellOption implements CordedInventoryOption {

    private final InventoryPos cords;
    private final Material material;

    private ItemSellOption(InventoryPos slot, Material material) {
        this.cords = slot;
        this.material = material;
    }

    static ItemSellOption itemSell(int slot, Material material) {
        return new ItemSellOption(InventoryPos.fromSlot(slot), material);
    }

    static ItemSellOption itemSell(int column, int row, Material material){
        return new ItemSellOption(new InventoryPos(column, row), material);
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public InventoryPos getPos() {
        return cords;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), makeItem(user.getMatData(getMaterial()), user.getSellAmount()));
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(SellShops.sell(user, getMaterial(), 1, user.getMatData(getMaterial())) > 0);
    }

    public static ItemStack makeItem(SoldMaterialData data, SellAmount sellAmount) {
        ItemStackBuilder builder = new ItemStackBuilder(data.getMaterial(), sellAmount.getValue());

        addLore(data, sellAmount, builder, 1);
        return builder.build();
    }

    public static void addLore(SoldMaterialData data, SellAmount amount, ItemStackBuilder builder, int scalar) {
        builder
                .addLore(
                        Component.text("Value: ")
                                .style(FtcFormatter.nonItalic(NamedTextColor.YELLOW))

                                .append(FtcFormatter.rhinesNonTrans(data.getPrice() * scalar))
                                .append(Component.text(" per item."))
                );

        if(data.isPriceSet()) {
            builder
                    .addLore(
                            Component.text("Original price: ")
                                    .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                                    .append(FtcFormatter.rhinesNonTrans(data.getOriginalPrice() * scalar))
                    );
        }

        builder
                .addLore(
                        FtcFormatter.rhinesNonTrans(data.getPrice() * 64 * scalar)
                                .style(FtcFormatter.nonItalic(NamedTextColor.GOLD))
                                .append(Component.text(" per stack."))
                )
                .addLore(
                        Component.text("Amount you will sell: ")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                                .append(amount.loreThing())
                )
                .addLore(
                        Component.text("Change the amount you will sell on the right")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                );
    }
}
