package net.forthecrown.cosmetics.options;

import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;

public class NoDeathOption implements InventoryOption {
    @Override
    public int getSlot() {
        return 31;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        Style gray = CrownItems.nonItalic(NamedTextColor.GRAY);

        ItemStackBuilder builder = new ItemStackBuilder(Material.BARRIER, 1)
                .setName(Component.text("No effect").style(CrownItems.nonItalic(NamedTextColor.GOLD)))
                .addLore(Component.text("Click to go back to default arrows").style(gray))
                .addLore(Component.text("without any effects").style(gray));

        AbstractDeathEffect effect = user.getCosmeticData().getActiveDeath();
        if(effect == null){
            builder
                    .addEnchant(Enchantment.CHANNELING, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(true);
        user.getCosmeticData().setActiveDeath(null);
    }
}
