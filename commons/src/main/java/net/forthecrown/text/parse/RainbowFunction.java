package net.forthecrown.text.parse;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

class RainbowFunction extends TextFunction {

  public RainbowFunction() {
    super(Pattern.compile("<\\s*rainbow\\s*[:=]\\s*((?:[^\\\\>]|\\\\>)+)\\s*>"));
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.GRADIENTS);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    String text = result.group(1);
    return Text.gradient(text, true, NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE);
  }
}
