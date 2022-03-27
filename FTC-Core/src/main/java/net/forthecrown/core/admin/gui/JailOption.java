package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import org.bukkit.Material;

record JailOption(PunishEntry entry,
                  JailCell cell,
                  InventoryPos pos
) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(
                getPos(),
                new ItemStackBuilder(Material.IRON_BARS, 1)
                        .setName(cell.key().value(), false)
                        .addLore("Pos: " + cell.getPos(), false)
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        BuiltInventory inventory = AdminGUI.createTimeSelection(entry, PunishType.JAIL, cell.key().asString());
        inventory.open(user);
    }
}