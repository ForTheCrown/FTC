package net.forthecrown.antigrief.ui;

import static net.forthecrown.antigrief.ui.AdminUi.ENTRY;
import static net.forthecrown.antigrief.ui.AdminUi.HEADER;
import static net.forthecrown.antigrief.ui.AdminUi.PAGE;
import static net.forthecrown.text.Text.nonItalic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.antigrief.StaffNote;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.page.ListPage;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class NotesPage extends ListPage<StaffNote> {

  public NotesPage(MenuPage parent) {
    super(parent, PAGE);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var builder = ItemStacks.builder(Material.BOOK)
        .setName("Staff notes");

    if (getList(user, context).isEmpty()) {
      builder.addLore("&cNo notes to view!");
    } else {
      builder.addLore("&7Notes added by staff members");
    }

    return builder.build();
  }

  @Override
  protected List<StaffNote> getList(User user, Context context) {
    var entry = context.get(ENTRY);
    return entry.getNotes();
  }

  @Override
  protected ItemStack getItem(User user, StaffNote note, Context context) {
    var builder = ItemStacks.builder(Material.MAP)
        .setNameRaw(
            Text.format("Note by: '{0}', written: {1, date}",
                nonItalic(NamedTextColor.GRAY),
                note.source(), note.issued()
            )
        );

    String[] words = note.info().split(" ");
    var writer = TextWriters.buffered();

    int lineLength = 0;

    for (String s : words) {
      lineLength += s.length();

      writer.write(s + " ");

      // Try to limit the amount of character
      // on one line to just 20
      // if more than 20 characters on current
      // line, move to next line
      if (lineLength >= 20) {
        writer.newLine();
        lineLength = 0;
      }
    }

    writer.newLine();
    builder.setLore(writer.getLore());

    return builder.build();
  }

  @Override
  protected void onClick(User user, StaffNote entry, Context context, ClickContext click)
      throws CommandSyntaxException
  {

  }

  @Override
  protected MenuNode createHeader() {
    return HEADER;
  }
}