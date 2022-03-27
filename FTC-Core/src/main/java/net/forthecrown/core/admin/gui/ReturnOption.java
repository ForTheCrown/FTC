package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

record ReturnOption(CrownUser entry) implements CordedInventoryOption {
    private static final InventoryPos POS = new InventoryPos(0, 0);

    @Override
    public InventoryPos getPos() {
        return POS;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(
                POS,
                new ItemStackBuilder(Material.PAPER)
                        .setName(Component.text("Return to main menu").style(nonItalic()))
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        BuiltInventory inventory = AdminGUI.createOveriew(entry);
        inventory.open(user);
    }
}