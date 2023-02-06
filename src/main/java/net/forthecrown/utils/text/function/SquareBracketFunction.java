package net.forthecrown.utils.text.function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.utils.text.TextFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class SquareBracketFunction extends TextFunction {
  private final String label;

  public SquareBracketFunction(int flags, String label) {
    super(
        flags,
        Pattern.compile("< ?" + label + " ?=[^>]+>")
    );

    this.label = label;
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException
  {
    String input = group.group();
    input = input.substring(1, input.length() - 1)
        .trim()
        .substring(label.length())
        .trim()
        .substring(1)
        .trim();

    return render(input, flags);
  }

  protected abstract @Nullable Component render(String input, int flags)
      throws CommandSyntaxException;
}