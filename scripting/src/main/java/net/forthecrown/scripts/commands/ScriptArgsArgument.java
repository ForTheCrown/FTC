package net.forthecrown.scripts.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class ScriptArgsArgument implements ArgumentType<String[]> {
  private ScriptArgsArgument() {}

  public static final ScriptArgsArgument SCRIPT_ARGS = new ScriptArgsArgument();

  @Override
  public String[] parse(StringReader reader) throws CommandSyntaxException {
    String string = reader.readString();
    return string.split("\s+");
  }
}