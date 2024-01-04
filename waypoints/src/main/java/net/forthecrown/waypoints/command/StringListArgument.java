package net.forthecrown.waypoints.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;

public class StringListArgument implements ArgumentType<List<String>>, SimpleVanillaMapped {

  @Override
  public List<String> parse(StringReader reader) throws CommandSyntaxException {
    var str = StringArgumentType.greedyString().parse(reader);
    var split = str.split("\\s+");
    return Lists.newArrayList(split);
  }

  @Override
  public ArgumentType<?> getVanillaType() {
    return StringArgumentType.greedyString();
  }
}
