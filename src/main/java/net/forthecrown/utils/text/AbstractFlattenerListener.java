package net.forthecrown.utils.text;

import java.util.Deque;
import java.util.LinkedList;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFlattenerListener implements FlattenerListener {
  protected final Deque<Style> styles = new LinkedList<>();
  protected Style style = Style.empty();

  @Override
  public void pushStyle(@NotNull Style style) {
    styles.push(style);
    flattenStyle();
  }

  @Override
  public void popStyle(@NotNull Style style) {
    styles.pop();
    flattenStyle();
  }

  protected void flattenStyle() {
    if (styles.isEmpty()) {
      style = Style.empty();
    }

    Style.Builder builder = Style.style();

    for (var s : styles) {
      builder.merge(s, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
    }

    style = builder.build();
  }
}