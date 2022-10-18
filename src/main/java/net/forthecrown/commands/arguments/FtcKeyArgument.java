package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;

public class FtcKeyArgument implements ArgumentType<String>, VanillaMappedArgument {
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        while (reader.canRead() && Keys.isValidKeyChar(reader.peek())) {
            reader.skip();
        }

        int end = reader.getCursor();

        return reader.getString().substring(start, end);
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return ScoreHolderArgument.scoreHolder();
    }
}