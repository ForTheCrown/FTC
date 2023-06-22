package net.forthecrown.antigrief.ui;

import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;

public final class AdminUi {
  private AdminUi() {}

  static final ContextSet SET = ContextSet.create();
  static final ContextOption<Integer> PAGE = SET.newOption(0);
  static final ContextOption<PunishEntry> ENTRY = SET.newOption();
  static final ContextOption<PunishBuilder> PUNISHMENT = SET.newOption();
  static final ContextOption<Integer> TIME_MULTIPLIER = SET.newOption(1);

  static final MenuNode HEADER = MenuNode.builder()
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