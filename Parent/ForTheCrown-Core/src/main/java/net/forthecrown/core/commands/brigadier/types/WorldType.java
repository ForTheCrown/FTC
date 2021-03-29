package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.ArgumentDimension;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.World;

public class WorldType {
    public static ArgumentDimension world(){
        return ArgumentDimension.a();
    }

    public static World getWorld(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return ArgumentDimension.a(c, argument).getWorld();
    }
}
