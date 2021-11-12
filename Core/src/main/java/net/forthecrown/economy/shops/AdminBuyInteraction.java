package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class AdminBuyInteraction implements ShopInteraction {
    @Override
    public void test(SignShopSession session, Economy economy) throws CommandSyntaxException {
        if(!session.customerHasSpace()) throw FtcExceptionProvider.inventoryFull(); //User has no space for items

        //User cannot afford shop
        if(!session.getCustomer().hasBalance(session.getPrice())) throw FtcExceptionProvider.cannotAfford(session.getPrice());
    }

    @Override
    public void interact(SignShopSession session, Economy economy) {
        ItemStack exampleItem = session.getExampleItem();
        ShopCustomer customer = session.getCustomer();

        //Bought item message
        customer.sendMessage(
                Component.translatable("shops.used.buy",
                        NamedTextColor.GRAY,
                        FtcFormatter.itemAndAmount(exampleItem).color(NamedTextColor.YELLOW),
                        FtcFormatter.rhines(session.getPrice()).color(NamedTextColor.GOLD)
                )
        );

        //Remove money
        customer.removeBalance(session.getPrice());

        //Give item
        int amount = exampleItem.getAmount();
        session.getCustomerInventory().addItem(exampleItem);

        //Tell the session that the amount of items exchanged changed
        session.growAmount(amount);
    }

    @Override
    public ShopType getType() {
        return ShopType.ADMIN_BUY;
    }
}
