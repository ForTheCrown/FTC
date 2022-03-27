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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.function.IntFunction;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record PageSwitchOption(int page, int mod,
                        IntFunction<BuiltInventory> pageConsumer,
                        InventoryPos pos
) implements CordedInventoryOption {

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.PAPER, 1)
                .setName(Component.text(mod == 1 ? ">> Next page >>" : "<< Previous Page <<").style(nonItalic(NamedTextColor.YELLOW)));

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        BuiltInventory inventory = pageConsumer.apply(page + mod);
        inventory.open(user);
    }
}