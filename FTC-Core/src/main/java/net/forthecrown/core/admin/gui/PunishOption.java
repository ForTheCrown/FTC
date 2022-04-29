package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
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
        ItemStackBuilder builder = new ItemStackBuilder(user.hasPermission(type.getPermission()) ?
                PunishmentOption.typeToMaterial(type) : Material.OBSIDIAN, 1)
                .setName("&f" + type.presentableName(), true);

        if(entry.isPunished(type)) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS)
                    .addLore(Component.text("User is already punished with this").style(nonItalic(NamedTextColor.RED)));
        }

        if(!user.hasPermission(type.getPermission())) {
            builder.addLore("&cYou do not have permission to use this", true);
        }

        CrownUser entryUser = entry.entryUser();
        if (!Punishments.canPunish(user.getCommandSource(), entryUser)) {
            builder.addLore("&cCannot punish " + entryUser.getName(), true);
        }

        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if(entry.isPunished(type)) {
            throw FtcExceptionProvider.create(user.getName() + " has already been " + type.nameEndingED());
        }

        if(!user.hasPermission(type.getPermission())) {
            throw FtcExceptionProvider.create("You do not have permission to use this");
        }

        CrownUser entryUser = entry.entryUser();
        if (!Punishments.canPunish(user.getCommandSource(), entryUser)) {
            throw FtcExceptionProvider.create("Cannot punish " + entryUser.getName());
        }

        PunishBuilder builder = new PunishBuilder(entry, type);

        BuiltInventory inventory = type == PunishType.JAIL ?
                AdminGUI.createJailSelector(builder) :
                AdminGUI.createTimeSelection(builder, 1);

        inventory.open(user);
    }
}