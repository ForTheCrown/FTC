package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Mth;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record NoteViewOption(PunishEntry entry, InventoryPos pos) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.MAP, Mth.clamp(entry.notes().size(), 1, Material.MAP.getMaxStackSize()))
                .setName(Component.text("Admin Notes"));

        if (entry.notes().isEmpty()) {
            builder.addLore(Component.text("No notes to view").style(nonItalic(NamedTextColor.RED)));
        }

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if (entry.notes().isEmpty()) return;

        BuiltInventory inventory = AdminGUI.createNoteView(entry, 0);
        inventory.open(user);
    }
}