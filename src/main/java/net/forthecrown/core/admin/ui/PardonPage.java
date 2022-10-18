package net.forthecrown.core.admin.ui;

import net.forthecrown.text.Text;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.core.admin.ui.AdminUI.ENTRY;
import static net.forthecrown.utils.inventory.menu.Menus.DEFAULT_INV_SIZE;

class PardonPage extends AdminUiPage {
    private final ItemStack
        CONFIRM_ITEM = ItemStacks.builder(Material.GREEN_STAINED_GLASS_PANE)
            .setName("Confirm pardon")
            .build(),

        DENY_ITEM = ItemStacks.builder(Material.RED_STAINED_GLASS_PANE)
            .setName("Deny pardon")
            .build();

    private final static Slot
        CONFIRM_SLOT = Slot.of(3, 1),
        DENY_SLOT = Slot.of(5, 1);

    private final Punishment punishment;

    public PardonPage(AdminUiPage parent, Punishment punishment) {
        super(Component.text("Pardon punishment?"), DEFAULT_INV_SIZE, parent);
        this.punishment = punishment;
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        builder.add(HEAD_SLOT.add(0, 2),
                MenuNode.builder()
                        // This needs to be a function because when this
                        // class is created, this createMenu function will
                        // be called before the `punishment` field is set
                        .setItem(user -> PunishmentListPage.createItem(punishment, true))
                        .build()
        );

        builder.add(CONFIRM_SLOT, option(true));
        builder.add(DENY_SLOT, option(false));
    }

    private MenuNode option(boolean pardon) {
        return MenuNode.builder()
                .setItem((user, context) -> pardon ? CONFIRM_ITEM : DENY_ITEM)

                .setRunnable((user, context, click) -> {
                    if (!pardon) {
                        getParent().onClick(user, context, click);
                        return;
                    }

                    var entry = context.get(ENTRY);
                    entry.revokePunishment(punishment.getType(), user.getName());

                    user.sendMessage(
                            Text.format("Pardoned {0, user}'s {1}",
                                    entry.getHolder(),
                                    punishment.getType().presentableName()
                            )
                    );

                    AdminUI.open(user, entry.getUser());
                })

                .build();
    }
}