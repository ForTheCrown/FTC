package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.nbt.NBT;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.TagParser;

import java.util.Collection;

public class NbtType implements ArgumentType<NBT> {
    private static final NbtType TYPE = new NbtType();
    private static final NbtTagArgument handle = NbtTagArgument.nbtTag();
    protected NbtType() {}

    public static NbtType nbt(){
        return TYPE;
    }

    @Override
    public NBT parse(StringReader reader) throws CommandSyntaxException {
        return NBT.of(new TagParser(reader).readStruct());
    }

    @Override
    public Collection<String> getExamples() {
        return handle.getExamples();
    }
}
