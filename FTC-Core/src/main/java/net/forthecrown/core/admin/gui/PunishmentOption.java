package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishment;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record PunishmentOption(Punishment punish,
                        PunishEntry entry,
                        boolean active, boolean runnable,
                        InventoryPos pos
) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(typeToMaterial(punish.type()))
                .setName(Component.text((active ? "Active" : "Past") + " punishment").style(nonItalic(NamedTextColor.WHITE)));

        ItemLoreBuilder loreBuilder = new ItemLoreBuilder();
        ComponentWriter writer = ComponentWriter.loreWriter(loreBuilder);
        punish.writeDisplay(writer);

        builder.addLore(loreBuilder.getLore());
        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if (!active || !runnable) return;

        BuiltInventory inventory = AdminGUI.createCurrentPunishmentView(punish, entry);
        inventory.open(user);
    }

    static Material typeToMaterial(PunishType type) {
        return switch (type) {
            case BAN -> Material.IRON_AXE;
            case JAIL -> Material.IRON_BARS;
            case MUTE -> Material.BARREL;
            case SOFT_MUTE -> Material.STRUCTURE_VOID;
            case IP_BAN -> Material.DIAMOND_AXE;
            case KICK -> Material.NETHERITE_BOOTS;
        };
    }
}