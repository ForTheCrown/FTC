package net.forthecrown.commands.economy;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.text.Messages;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.sell.SellShopMenu;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandShop extends FtcCommand {

    public CommandShop() {
        super("Shop");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Shop
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    var user = getUserSender(c);
                    Crown.getEconomy().getSellShop().getMainMenu().open(user);
                    return 0;
                })

                .then(argument("menu", RegistryArguments.SELLS_SHOP)
                        .executes(c -> {
                            var user = getUserSender(c);
                            Holder<SellShopMenu> menu = c.getArgument("menu", Holder.class);

                            menu.getValue().getInventory().open(user);
                            return 0;
                        })
                )

                .then(literal("web")
                        .executes(c -> {
                            c.getSource().sendMessage(Messages.SHOP_WEB_MESSAGE);
                            return 0;
                        })
                );
    }


}