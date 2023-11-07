package net.forthecrown.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;
import net.forthecrown.registry.Registries;
import net.minecraft.commands.arguments.ScoreHolderArgument;

public class FtcKeyArgument implements ArgumentType<String>, SimpleVanillaMapped {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();

    while (reader.canRead() && Registries.isValidKeyChar(reader.peek())) {
      reader.skip();
    }

    int end = reader.getCursor();

    if (start == end) {
      throw Grenadier.exceptions().invalidKey("", reader);
    }
    return reader.getString().substring(start, end);
  }

  @Override
  public ArgumentType<?> getVanillaType() {
    return ScoreHolderArgument.scoreHolder();
  }
}