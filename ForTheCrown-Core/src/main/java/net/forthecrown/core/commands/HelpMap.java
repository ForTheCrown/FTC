package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

public class HelpMap extends CrownCommandBuilder {
    public HelpMap(){
        super("map", FtcCore.getInstance());

        setAliases("worldmap");
        setPermission(null);

        register();
    }

    /*
     * Sends the player the dynmap link
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            CommandSender sender = c.getSource().getBukkitSender();
            sender.sendMessage(ChatColor.GRAY + "Dynmap link:");

            TextComponent text = Component.text("mc.forthecrown.net:3140/").color(NamedTextColor.AQUA);
            text = text.clickEvent(ClickEvent.openUrl("http://mc.forthecrown.net:3140/"));
            text = text.hoverEvent(HoverEvent.showText(Component.text("Opens the server's dynmap.")));
            sender.sendMessage(text);
            return 0;
        });
    }
}
