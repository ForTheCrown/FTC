package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

record TimeMultiplierOption(PunishBuilder builder, int multiplier, boolean active, InventoryPos pos) implements CordedInventoryOption {
    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE, multiplier)
                .setName(ChatUtils.wrapForItems(Component.text("Time multiplier " + multiplier + "x")))
                .addLore("Sets the punishment time multipler to " + multiplier + "x", true);

        if (active) {
            builder.addEnchant(Enchantment.VANISHING_CURSE, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        inventory.setItem(pos, builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if (active) return;
        AdminGUI.createTimeSelection(builder, multiplier).open(user);
    }
}