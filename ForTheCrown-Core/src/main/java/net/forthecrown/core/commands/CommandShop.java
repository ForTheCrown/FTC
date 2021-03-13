package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
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
     * Referenced other classes:
     * - SellShop
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .executes(c ->{
                    Player player = getPlayerSender(c);
                    player.openInventory(new SellShop(player).mainMenu());
                    return 0;
                })
                .then(argument("drops")
                    .executes(c -> {
                        Player player = getPlayerSender(c);
                        player.openInventory(new SellShop(player).dropsMenu());
                        return 0;
                    })
                )
                .then(argument("mining")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            player.openInventory(new SellShop(player).miningMenu());
                            return 0;
                        })
                )
                .then(argument("crops")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            player.openInventory(new SellShop(player).farmingMenu());
                            return 0;
                        })
                )
                .then(argument("web")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            user.sendMessage("&7Our webstore:");

                            TextComponent text = Component.text("https://for-the-crown.tebex.io/");
                            text = text.color(NamedTextColor.AQUA);
                            text = text.clickEvent(ClickEvent.openUrl("https://for-the-crown.tebex.io/"));
                            text = text.hoverEvent(HoverEvent.showText(Component.text("Opens the server's webstore")));

                            user.sendMessage(text);
                            return 0;
                        })
                );
    }
}
