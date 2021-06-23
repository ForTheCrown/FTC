package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NBT;
import net.minecraft.server.v1_16_R3.ArgumentNBTTag;

import java.util.Collection;

public class NbtType implements ArgumentType<NBT> {
    private static final NbtType TYPE = new NbtType();
    protected NbtType() {}

    public static NbtType nbt(){
        return TYPE;
    }

    @Override
    public NBT parse(StringReader reader) throws CommandSyntaxException {
        return NBT.of(ArgumentNBTTag.a().parse(reader));
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentNBTTag.a().getExamples();
    }
}
