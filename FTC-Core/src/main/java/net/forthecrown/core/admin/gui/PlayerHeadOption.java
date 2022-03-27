package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.ProfilePrinter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemLoreBuilder;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record PlayerHeadOption(CrownUser punished) implements CordedInventoryOption {
    private static final InventoryPos POS = new InventoryPos(4, 0);

    @Override
    public InventoryPos getPos() {
        return POS;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                .setProfile(punished)
                .setName(punished.nickDisplayName().style(nonItalic()));

        ItemLoreBuilder loreBuilder = new ItemLoreBuilder();
        ComponentWriter writer = ComponentWriter.loreWriter(loreBuilder);
        ProfilePrinter printer = new ProfilePrinter(punished, true, true, writer);
        printer.printFull();

        writer.newLine();
        builder.addLore(loreBuilder.getLore());

        inventory.setItem(POS, builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {}
}