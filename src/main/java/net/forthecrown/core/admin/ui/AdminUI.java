package net.forthecrown.core.admin.ui;

import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.context.ContextOption;
import net.forthecrown.utils.inventory.menu.context.ContextSet;

public class AdminUI {
    static final ContextSet SET = ContextSet.create();

    static final ContextOption<PunishEntry>     ENTRY           = SET.newOption();
    static final ContextOption<PunishBuilder>   PUNISHMENT      = SET.newOption();
    static final ContextOption<Integer>         PAGE            = SET.newOption(0);
    static final ContextOption<Integer>         TIME_MULTIPLIER = SET.newOption(1);

    static final MainPage MAIN = new MainPage();

    public static void open(User viewer, User entry) {
        var pEntry = Punishments.get().getEntry(entry.getUniqueId());

        var context = SET.createContext();
        context.set(ENTRY, pEntry);

        MAIN.getInventory().open(viewer, context);
    }
}