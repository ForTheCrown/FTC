package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Collection;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record ViewPunishmentsOption(PunishEntry entry,
                             InventoryPos pos,
                             boolean past
) implements CordedInventoryOption {

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.CHEST, 1)
                .setName(Component.text(text() + " punishments").style(nonItalic()));

        if (getPunishments().isEmpty()) {
            builder.addLore(Component.text("This user has no " + text() + " punishments", nonItalic(NamedTextColor.RED)));
        }

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        Collection<Punishment> punishments = getPunishments();
        if (punishments.isEmpty()) {
            throw FtcExceptionProvider.create("This user has no " + text() + " punishments to display");
        }

        BuiltInventory inventory = AdminGUI.createPunishmentsView(entry, past, 0);
        inventory.open(user);
    }

    private String text() {
        return past ? "Past" : "Current";
    }

    private Collection<Punishment> getPunishments() {
        return past ? entry.past() : entry.current();
    }
}