package net.forthecrown.command.settings;

import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.forthecrown.utils.BookBuilder;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;

public class SettingsBook<C> {

  @Getter
  private final List<BookSetting<C>> settings = new ObjectArrayList<>();

  @Getter @Setter
  private Component title = Component.text("Settings");

  public void open(C c, User user) {
    for (var s: settings) {
      s.setBook(this);
    }

    var book = createBook(c, title, settings);
    user.openBook(book);
  }

  public static <C> Book createBook(
      C context,
      Component title,
      Collection<? extends BookSetting<C>> settings
  ) {
    BookBuilder builder = new BookBuilder()
        .title(title)
        .addCentered(title)
        .setAuthor("")
        .addEmptyLine();

    for (var option : settings) {
      if (!option.shouldInclude(context)) {
        continue;
      }

      Component header = option.displayName().append(text(":"));
      Component options = option.createButtons(context);

      builder.addField(header, options);
    }

    return builder.build();
  }
}