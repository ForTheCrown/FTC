package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record PardonOption(PunishEntry entry,
                    Punishment punishment,
                    InventoryPos pos,
                    boolean pardon
) implements CordedInventoryOption {

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(pardon ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                .setName(Component.text(pardon ? "Pardon" : "Cancel").style(nonItalic()));

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if (pardon) {
            entry.revokePunishment(punishment.type());

            Punishments.announcePardon(user.getCommandSource(), entry.entryUser(), punishment.type());
        }

        BuiltInventory inventory;

        if (entry.current().isEmpty()) {
            inventory = AdminGUI.createOveriew(entry.entryUser());
        } else {
            inventory = AdminGUI.createPunishmentsView(entry, false, 0);
        }

        inventory.open(user);
    }
}