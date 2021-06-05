package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class HelpMap extends FtcCommand {
    public HelpMap(){
        super("map", CrownCore.inst());

        setAliases("worldmap");
        setPermission(Permissions.HELP);
        setDescription("Shows the dynmap link");

        register();
    }

    /*
     * Sends the player the dynmap link
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CommandSender sender = c.getSource().asBukkit();
            sender.sendMessage(Component.translatable("commands.dynmap", NamedTextColor.GRAY));

            TextComponent text = Component.text("mc.forthecrown.net:3140/").color(NamedTextColor.AQUA);
            text = text.clickEvent(ClickEvent.openUrl("http://mc.forthecrown.net:3140/"));
            text = text.hoverEvent(Component.translatable("commands.dynmap.hover"));
            sender.sendMessage(text);
            return 0;
        });
    }
}
