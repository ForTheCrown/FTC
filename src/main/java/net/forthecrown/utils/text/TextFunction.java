package net.forthecrown.utils.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class TextFunction {
  private final int flags;

  @RegExp
  private final String pattern;

  public TextFunction(int flags, @RegExp String pattern) {
    this.flags = flags;
    this.pattern = pattern;
  }

  public TextFunction(int flags, Pattern pattern) {
    this(flags, pattern.pattern());
  }

  public abstract @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException;
}