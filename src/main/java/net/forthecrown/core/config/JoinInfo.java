package net.forthecrown.core.config;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;

@ConfigData(filePath = "joininfo.json")
public @UtilityClass class JoinInfo {

  public Info endInfo = create("&5The End &eis open!");
  public Info info = new Info();
  public Info endWeek = new Info();

  public Component display() {
    TextWriter writer = TextWriters.newWriter();

    info.write(writer);
    endInfo.write(writer);
    endWeek.write(writer);

    return writer.asComponent();
  }

  private static Info create(String text) {
    Info info = new Info();
    info.text = Text.renderString(text);
    return info;
  }

  public class Info {

    public Component text = Component.empty();
    public boolean visible = false;

    void write(TextWriter writer) {
      if (!visible
          || text == null
          || Objects.equals(text, Component.empty())
      ) {
        return;
      }

      writer.line(text);
    }
  }
}