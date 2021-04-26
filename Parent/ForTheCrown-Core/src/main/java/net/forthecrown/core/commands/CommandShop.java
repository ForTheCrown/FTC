package net.forthecrown.core.commands;

import com.mojang.brigadier.Command;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.LiteralArgument;
import net.forthecrown.core.inventories.SellShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class CommandShop extends CrownCommandBuilder {

    public CommandShop(){
        super("shop", FtcCore.getInstance());

        setUsage("&7Usage:&r /shop [mining | farming | drops]");
        setDescription("Opens the Shop GUI in which one can sell things");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the ShopGUI and allows players to sell their items
     *
     *
     * Valid usages of command:
     * - /shop
     * - /shop <farming | mining | drops | web>
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .executes(cmd(SellShop.Menu.MAIN))

                .then(arg(SellShop.Menu.DROPS))
                .then(arg(SellShop.Menu.MINING))
                .then(arg(SellShop.Menu.MINING_BLOCKS))
                .then(arg(SellShop.Menu.CROPS))

                .then(argument("web")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            user.sendMessage("&7Our webstore:");

                            TextComponent text = Component.text("https://for-the-crown.tebex.io/").color(NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.openUrl("https://for-the-crown.tebex.io/"))
                                    .hoverEvent(HoverEvent.showText(Component.text("Opens the server's webstore")));

                            user.sendMessage(text);
                            return 0;
                        })
                );
    }

    private Command<CommandListenerWrapper> cmd(SellShop.Menu menu){
        return c -> {
            Player player = getPlayerSender(c);
            player.openInventory(new SellShop(player).open(menu));
            return 0;
        };
    }

    private LiteralArgument arg(SellShop.Menu menu){
        return argument(menu.toString().toLowerCase()).executes(cmd(menu));
    }
}