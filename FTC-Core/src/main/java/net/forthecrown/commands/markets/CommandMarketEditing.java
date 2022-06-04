package net.forthecrown.commands.markets;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarketEditing extends FtcCommand {

    public CommandMarketEditing() {
        super("MarketEditing");

        setAliases("toggleshopediting", "togglemarketediting");
        setPermission(Permissions.MARKETS);
        setDescription("Allows/disallows shop member to edit sign shops");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /MarketEditing
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);

            if (!user.getMarketData().currentlyOwnsShop()) {
                throw FtcExceptionProvider.noShopOwned();
            }

            Markets markets = Crown.getMarkets();
            MarketShop shop = markets.get(user.getUniqueId());

            boolean state = !shop.isMemberEditingAllowed();
            shop.setMemberEditingAllowed(state);

            user.sendMessage(
                    Component.translatable("market.memberEdit." + state, state ? NamedTextColor.YELLOW : NamedTextColor.GRAY)
            );
            return 0;
        });
    }
}