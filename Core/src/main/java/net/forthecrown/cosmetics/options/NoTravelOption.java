package net.forthecrown.cosmetics.options;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public class NoTravelOption implements CordedInventoryOption {
    private final InventoryPos pos;

    public NoTravelOption() {
        pos = new InventoryPos(3, 3);
    }

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.BARRIER)
                .setName(Component.text("No effect").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))

                .addLore(Component.text("Click to go back to default").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                .addLore(Component.text("Region pole travelling, without any effects").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));

        if(!user.getCosmeticData().hasActiveTravel()) {
            builder
                    .addEnchant(Enchantment.CHANNELING, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        context.setReloadInventory(true);
        user.getCosmeticData().setActiveTravel(null);
    }
}
