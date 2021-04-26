package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.core.nbt.NBT;
import net.minecraft.server.v1_16_R3.*;

public class NbtType {

    public static ArgumentNBTTag tag(){
        return ArgumentNBTTag.a();
    }

    public static ArgumentNBTBase base(){
        return ArgumentNBTBase.a();
    }

    public static NBTTagCompound getTag(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentNBTTag.a(c, argument);
    }

    public static NBT getNBT(CommandContext<CommandListenerWrapper> c, String argument){
        return NBT.of(getTag(c, argument));
    }

    public static NBTBase getBase(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentNBTBase.a(c, argument);
    }
}
