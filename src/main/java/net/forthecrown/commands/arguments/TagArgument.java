package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.string.TagParseException;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.commands.arguments.NbtTagArgument;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TagArgument<T extends BinaryTag>
    implements ArgumentType<T>, VanillaMappedArgument {

  private final Function<StringReaderWrapper, T> reader;

  @Override
  public T parse(StringReader reader) throws CommandSyntaxException {
    var wrappedReader = StringReaderWrapper.wrap(reader);

    try {
      return this.reader.apply(wrappedReader);
    } catch (TagParseException exc) {
      exc.setContext(null);
      exc.setParseOffset(0);

      if (exc.getCause() != null) {
        Loggers.getLogger().error("Error parsing tag", exc);
      }

      throw Exceptions.formatWithContext(exc.getMessage(), reader);
    }
  }

  @Override
  public ArgumentType<?> getVanillaArgumentType() {
    return NbtTagArgument.nbtTag();
  }
}