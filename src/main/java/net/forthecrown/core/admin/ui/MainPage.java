package net.forthecrown.core.admin.ui;

import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.text;

class MainPage extends AdminUiPage {
    private static final Component TITLE = text("Admin GUI");

    static final Slot
        PUNISH_USER_SLOT = Slot.of(4, 2),
        NOTES_SLOT       = Slot.of(4, 1),

        PAST_PUNISHMENTS_SLOT = Slot.of(2, 1),
        CURRENT_PUNISHMENTS_SLOT = Slot.of(6, 1);

    public MainPage() {
        super(TITLE, 27, null);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        var punishPage = new PunishPage(this);
        var noteView = new NotesPage(this);
        var pastView = new PunishmentListPage(this, false);
        var currentView = new PunishmentListPage(this, true);

        builder.add(PUNISH_USER_SLOT, punishPage);
        builder.add(NOTES_SLOT, noteView);

        builder.add(PAST_PUNISHMENTS_SLOT, pastView);
        builder.add(CURRENT_PUNISHMENTS_SLOT, currentView);
    }
}