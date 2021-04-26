package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentTime;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class TimeType {
    public static ArgumentTime time(){
        return ArgumentTime.a();
    }

    public static int getTime(CommandContext<CommandListenerWrapper> c, String argument){
        return c.getArgument(argument, Integer.class);
    }
}
