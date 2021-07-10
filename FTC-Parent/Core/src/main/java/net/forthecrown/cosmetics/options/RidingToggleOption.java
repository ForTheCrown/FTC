package net.forthecrown.cosmetics.options;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class RidingToggleOption implements InventoryOption {
    @Override
    public int getSlot() {
        return 49;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        boolean allows = user.allowsRiding();

        ItemStackBuilder builder = new ItemStackBuilder(allows ? Material.SADDLE : Material.BARRIER)
                .addLore(Component.empty())
                .addLore(Component.text("Right-click someone to jump on top of them.").style(ChatFormatter.nonItalic(NamedTextColor.GRAY)))
                .addLore(Component.text("Shift-right-click someone to kick them off.").style(ChatFormatter.nonItalic(NamedTextColor.GRAY)))
                .addLore(Component.empty());

        if(allows){
            builder
                    .setName(Component.text("You can ride other players!").style(ChatFormatter.nonItalic(NamedTextColor.YELLOW)))
                    .addLore(Component.text("Click to disabled this feature.").style(ChatFormatter.nonItalic(NamedTextColor.GRAY)));
        } else {
            builder
                    .setName(Component.text("You've disabled riding other players!").style(ChatFormatter.nonItalic(NamedTextColor.YELLOW)))
                    .addLore(Component.text("Click to enable this feature.").style(ChatFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        inventory.setItem(getSlot(), builder.build());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        context.setReloadInventory(true);
        user.setAllowsRiding(!user.allowsRiding());
    }
}
