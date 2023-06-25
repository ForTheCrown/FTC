package net.forthecrown.antigrief.ui;

import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.inventory.ItemStacks;

public final class AdminUi {
  private AdminUi() {}

  static final ContextSet SET = ContextSet.create();
  static final ContextOption<Integer> PAGE = SET.newOption(0);
  static final ContextOption<PunishEntry> ENTRY = SET.newOption();
  static final ContextOption<PunishBuilder> PUNISHMENT = SET.newOption();
  static final ContextOption<Integer> TIME_MULTIPLIER = SET.newOption(1);

  static final MenuNode HEADER = MenuNode.builder()
      .setItem((user, context) -> {
        PunishEntry entry = context.getOrThrow(ENTRY);
        User target = entry.getUser();

        var builder = ItemStacks.headBuilder()
            .setProfile(target.getProfile())
            .setName(target.displayName(user));

        var writer = TextWriters.buffered();
        Users.getService().getNameFactory().writeProfileDisplay(writer, target, user);
        builder.addLore(writer.getBuffer());

        return builder.build();
      })

      .build();

  private static final MainPage MAIN_PAGE = new MainPage();

  public static void open(User viewer, User target) {
    Context ctx = SET.createContext();

    PunishEntry entry = Punishments.entry(target);
    assert entry != null;
    ctx.set(ENTRY, entry);

    MAIN_PAGE.getMenu().open(viewer, ctx);
  }
}