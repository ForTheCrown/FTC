package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public class SellInteraction implements ShopInteraction {
    @Override
    public void test(SignShopSession session, Economy economy) throws CommandSyntaxException {
        ShopType.ADMIN_SELL.getInteraction().test(session, economy); //Check basic stuff

        //Check the shop's owner can afford the shop
        if(!economy.has(session.getOwnership().getOwner(), session.getPrice())) throw FtcExceptionProvider.shopOwnerCannotAfford(session.getPrice());

        //Check shop has space for any more items
        if(!session.shopHasSpace()) throw FtcExceptionProvider.noShopSpace();
    }

    @Override
    public void interact(SignShopSession session, Economy economy) {
        ShopType.ADMIN_SELL.getInteraction().interact(session, economy);

        CrownUser owner = session.getOwnership().ownerUser();
        ItemStack example = session.getExampleItem();

        //Add item to shop, give user mulaa
        session.getShopInventory().addItem(example.clone());
        economy.add(owner.getUniqueId(), session.getPrice(), owner.getFaction() != Faction.PIRATES);

        //When session expires, tell the owner what occurred lol
        session.onSessionExpire(() -> {
            int totalEarned = session.getPrice() * (session.getAmount() / session.getExampleItem().getAmount());

            owner.sendMessage(
                    Component.translatable("shops.used.sell.owner",
                            NamedTextColor.GRAY,

                            session.getCustomer().shopDisplayName().color(NamedTextColor.YELLOW),
                            FtcFormatter.itemAndAmount(session.getExampleItem(), session.getAmount()).color(NamedTextColor.GOLD),
                            FtcFormatter.rhines(totalEarned).color(NamedTextColor.YELLOW)
                    )
            );
        });
    }

    @Override
    public ShopType getType() {
        return ShopType.SELL;
    }
}
