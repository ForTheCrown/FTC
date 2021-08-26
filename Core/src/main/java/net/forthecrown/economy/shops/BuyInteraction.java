package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class BuyInteraction implements ShopInteraction {
    @Override
    public void test(SignShopSession session, Balances balances) throws CommandSyntaxException {
        ShopType.ADMIN_BUY.getInteraction().test(session, balances); //Check they pass the basic stuff

        //Shop stock check
        if(session.getShop().isOutOfStock()) throw FtcExceptionProvider.shopOutOfStock();

        ItemStack example = session.getExampleItem();

        //Shop stock check, but better
        if(!session.getInventory().containsAtLeast(example, example.getAmount())) {
            session.getShop().setOutOfStock(true);
            throw FtcExceptionProvider.shopOutOfStock();
        }
    }

    @Override
    public void interact(SignShopSession session, Balances balances) {
        ShopType.ADMIN_BUY.getInteraction().interact(session, balances); //Change the basic stuff

        CrownUser owner = session.getOwner();
        ItemStack example = session.getExampleItem();

        //Add money to owner, remove item from shop
        balances.add(session.getOwner().getUniqueId(), session.getPrice(), owner.getFaction() != Faction.PIRATES);
        session.getInventory().removeItem(example.clone());

        //Check stock
        if(!session.getInventory().containsAtLeast(example, example.getAmount())) {
            session.getShop().setOutOfStock(true);
            ShopManager.informOfStockIssue(owner, session.getShop());
        }

        //When the session expires, tell the shop's owner
        session.onSessionExpire(() -> {
            //Get the total earned amount from the session
            int totalEarned = session.getPrice() * (session.getAmount() / session.getExampleItem().getAmount());

            owner.sendMessage(
                    Component.translatable("shops.used.buy.owner",
                            NamedTextColor.GRAY,

                            session.getUser().nickDisplayName().color(NamedTextColor.YELLOW),
                            FtcFormatter.itemAndAmount(example, session.getAmount()).color(NamedTextColor.GOLD),
                            FtcFormatter.rhines(totalEarned).color(NamedTextColor.YELLOW)
                    )
            );
        });
    }

    @Override
    public ShopType getType() {
        return ShopType.BUY;
    }
}
