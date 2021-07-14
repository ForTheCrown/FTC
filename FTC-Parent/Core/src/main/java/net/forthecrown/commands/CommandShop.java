package net.forthecrown.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.selling.SellShops;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.user.CrownUser;

import static net.forthecrown.economy.selling.SellShops.WEB_MESSAGE;

public class CommandShop extends FtcCommand {

    public CommandShop(){
        super("shop", CrownCore.inst());

        setDescription("Opens the Shop GUI in which one can sell things");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the ShopGUI and allows players to sell their items
     *
     * Valid usages of command:
     * - /shop
     * - /shop <farming | mining | mining_blocks | minerals | drops | web>
     *
     * Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(cmd(SellShops.MAIN))

                .then(arg("farming", SellShops.CROPS))
                .then(arg("drops", SellShops.DROPS))
                .then(arg("mining", SellShops.MINING))
                .then(arg("mining_blocks", SellShops.CRAFTABLE_BLOCKS))
                .then(arg("minerals", SellShops.MINERALS))

                .then(literal("web")
                        .executes(c -> {
                            c.getSource().sendMessage(WEB_MESSAGE);
                            return 0;
                        })
                );
    }

    private Command<CommandSource> cmd(BuiltInventory inventory) {
        return c -> {
            CrownUser user = getUserSender(c);

            inventory.open(user);
            return 0;
        };
    }

    private LiteralArgumentBuilder<CommandSource> arg(String name, BuiltInventory inventory) {
        return literal(name).executes(cmd(inventory));
    }
}