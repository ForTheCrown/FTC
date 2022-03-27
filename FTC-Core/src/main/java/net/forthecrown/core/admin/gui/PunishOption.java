package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record PunishOption(PunishEntry entry, PunishType type, InventoryPos pos) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(PunishmentOption.typeToMaterial(type), 1)
                .setName(type.presentableName(), false);

        if(entry.isPunished(type)) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS)
                    .addLore(Component.text("User is already punished with this").style(nonItalic(NamedTextColor.WHITE)));
        }

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if(entry.isPunished(type)) {
            throw FtcExceptionProvider.create(user.getName() + " has already been " + type.presentableName() +
                    (type.name().endsWith("E") ? "d" : "ed")
            );
        }

        BuiltInventory inventory = type == PunishType.JAIL ?
                AdminGUI.createJailSelector(entry) :
                AdminGUI.createTimeSelection(entry, type, null);

        inventory.open(user);
    }
}