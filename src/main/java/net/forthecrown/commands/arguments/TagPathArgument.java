package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.nbt.path.PathParseException;
import net.forthecrown.nbt.path.TagPath;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.commands.arguments.NbtPathArgument;

public class TagPathArgument
    implements ArgumentType<TagPath>, VanillaMappedArgument {

  TagPathArgument() {
  }

  @Override
  public TagPath parse(StringReader reader) throws CommandSyntaxException {
    var wrapped = StringReaderWrapper.wrap(reader);

    try {
      return TagPath.parse(wrapped);
    } catch (PathParseException exc) {
      exc.setPosition(0);
      exc.setContext(null);

      if (exc.getCause() != null) {
        Loggers.getLogger().error("Error running path parse", exc.getCause());
      }

      throw Exceptions.formatWithContext(exc.getMessage(), reader);
    }
  }

  @Override
  public ArgumentType<?> getVanillaArgumentType() {
    return NbtPathArgument.nbtPath();
  }
}