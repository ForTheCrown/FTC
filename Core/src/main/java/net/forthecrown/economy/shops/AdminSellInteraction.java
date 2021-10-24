package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class AdminSellInteraction implements ShopInteraction {
    @Override
    public void test(SignShopSession session, Economy economy) throws CommandSyntaxException {
        ItemStack example = session.getExampleItem();

        //User does not have item to sell
        if(!session.getPlayerInventory().containsAtLeast(example, example.getAmount())) throw FtcExceptionProvider.dontHaveItemForShop(example);
    }

    @Override
    public void interact(SignShopSession session, Economy economy) {
        ItemStack example = session.getExampleItem();

        //Add money and remove item
        economy.add(session.getCustomer().getUniqueId(), session.getPrice());
        session.getPlayerInventory().removeItemAnySlot(example.clone());

        //Tell em
        session.getCustomer().sendMessage(
                Component.translatable("shops.used.sell", NamedTextColor.GRAY,
                        FtcFormatter.itemAndAmount(example).color(NamedTextColor.YELLOW),
                        FtcFormatter.rhines(session.getPrice()).color(NamedTextColor.GOLD)
                )
        );

        session.growAmount(example.getAmount());
    }

    @Override
    public ShopType getType() {
        return ShopType.ADMIN_SELL;
    }
}
