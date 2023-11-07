package net.forthecrown.text;

import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BufferedTextWriter extends DefaultTextWriter {

  private List<Component> buf;
  private TextComponent.Builder line;

  public BufferedTextWriter(List<Component> buf) {
    super(
        Component.text()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    );
    this.buf = buf;
    line = createBuilder();
  }

  @Override
  protected void onNewLine() {
    super.onNewLine();

    buf.add(line.build());
    line = createBuilder();
  }

  @Override
  protected void onWrite(Component text) {
    super.onWrite(text);
    line.append(text);
  }

  @Override
  protected void onClear() {
    super.onClear();
    buf.clear();
    line = createBuilder();
  }

  public List<Component> getBuffer() {
    if (!lineEmpty) {
      buf.add(line.build());
      line = createBuilder();
    }

    return buf;
  }

  private TextComponent.Builder createBuilder() {
    var style = asComponent().style();

    return Component.text()
        .style(style)
        .colorIfAbsent(NamedTextColor.WHITE)
        .decoration(
            TextDecoration.ITALIC,
            style.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE
                ? TextDecoration.State.TRUE : TextDecoration.State.FALSE
        );
  }

  @Override
  public void setColor(TextColor color) {
    super.setColor(color);

    buf = buf.stream()
        .map(component -> component.color(color))
        .collect(Collectors.toList());
  }

  @Override
  public void setStyle(Style style) {
    super.setStyle(style);

    buf = buf.stream()
        .map(component -> component.style(style))
        .collect(Collectors.toList());
  }
}