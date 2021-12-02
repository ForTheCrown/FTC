package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.util.Collection;

public class NBTArgument implements ArgumentType<CompoundTag> {
    private static final NBTArgument TYPE = new NBTArgument();
    private static final NbtTagArgument handle = NbtTagArgument.nbtTag();
    protected NBTArgument() {}

    public static NBTArgument nbt(){
        return TYPE;
    }

    public static CompoundTag get(CommandContext<CommandSource> c, String arg) {
        return c.getArgument(arg, CompoundTag.class);
    }

    @Override
    public CompoundTag parse(StringReader reader) throws CommandSyntaxException {
        return new TagParser(reader).readStruct();
    }

    @Override
    public Collection<String> getExamples() {
        return handle.getExamples();
    }

    public NbtTagArgument getHandle() {
        return handle;
    }
}
