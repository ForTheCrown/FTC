package net.forthecrown.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.SellShop;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

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
     *
     * Valid usages of command:
     * - /shop
     * - /shop <farming | mining | drops | web>
     *
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(cmd(SellShop.Menu.MAIN))

                .then(arg(SellShop.Menu.DROPS))
                .then(arg(SellShop.Menu.MINING))
                .then(arg(SellShop.Menu.MINING_BLOCKS))
                .then(arg(SellShop.Menu.CROPS))

                .then(literal("web")
                        .executes(c -> {
                            c.getSource().sendMessage(WEB_MESSAGE);
                            return 0;
                        })
                );
    }

    public static final Component WEB_MESSAGE = Component.text()
            .append(Component.translatable("commands.shop.web", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("https://for-the-crown.tebex.io/").color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("https://for-the-crown.tebex.io/"))
                    .hoverEvent(Component.translatable("commands.shop.web.hover")))
            .build();

    private Command<CommandSource> cmd(SellShop.Menu menu){
        return c -> {
            Player player = getPlayerSender(c);
            player.openInventory(new SellShop(player).open(menu));
            return 0;
        };
    }

    private LiteralArgumentBuilder<CommandSource> arg(SellShop.Menu menu){
        return literal(menu.toString().toLowerCase()).executes(cmd(menu));
    }
}