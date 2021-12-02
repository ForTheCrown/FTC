package net.forthecrown.cosmetics.options;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public class CosmeticHeader implements InventoryOption {
    @Override
    public int getSlot() {
        return 4;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.NETHER_STAR, 1)
                .setName(Component.text("Menu").style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)))
                .addLore(Component.empty())
                .addLore(
                        Component.text("You have ")
                                .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                                .append(Component.text(user.getGems() + " Gems").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
                                .append(Component.text("."))
                );

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
    }
}
