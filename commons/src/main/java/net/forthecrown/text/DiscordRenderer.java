package net.forthecrown.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class DiscordRenderer extends AbstractFlattenerListener {
  private StringBuilder builder = new StringBuilder();

  String prefix = "";

  public String flatten(Component component) {
    TextSplitter.FLATTENER.flatten(component, this);
    return toString();
  }

  @Override
  public void component(@NotNull String text) {
    builder.append(prefix)
        .append(text)
        .append(StringUtils.reverse(prefix));
  }

  @Override
  protected void flattenStyle() {
    super.flattenStyle();

    boolean italic = style.hasDecoration(TextDecoration.ITALIC);
    boolean bold = style.hasDecoration(TextDecoration.BOLD);
    boolean underlined = style.hasDecoration(TextDecoration.UNDERLINED);
    boolean strikethrough = style.hasDecoration(TextDecoration.STRIKETHROUGH);

    prefix = ""
        + (italic ? "*" : "")
        + (bold ? "**" : "")
        + (underlined ? "__" : "")
        + (strikethrough ? "~~" : "");
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}