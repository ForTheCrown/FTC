package net.forthecrown.core.commands.brigadier;

import net.minecraft.server.v1_16_R3.CommandDispatcher;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.plugin.Plugin;

//cool name lul
public class RoyalBrigadier {

    private static CommandDispatcher dispatcher;

    private final Plugin plugin;

    public RoyalBrigadier(Plugin plugin){
        this.plugin = plugin;

        dispatcher = MinecraftServer.getServer().getCommandDispatcher();
    }

    public void loadCommands(){

    }

    public static CommandDispatcher getDispatcher() {
        return dispatcher;
    }
}
