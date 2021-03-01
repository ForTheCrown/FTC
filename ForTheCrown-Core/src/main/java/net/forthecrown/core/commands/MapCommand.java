package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

public class MapCommand extends CrownCommandBuilder {
    public MapCommand(){
        super("map", FtcCore.getInstance());

        setAliases("worldmap");
        setPermission(null);

        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            CommandSender sender = c.getSource().getBukkitSender();
            sender.sendMessage(ChatColor.GRAY + "Dynmap link:");
            sender.sendMessage(ChatColor.AQUA +"http://mc.forthecrown.net:3140/");
            return 0;
        });
    }
}
