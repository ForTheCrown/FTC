package net.forthecrown.cosmetics.effects;

import net.forthecrown.inventory.custom.CustomInventory;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Sound;

/**
 * Final methods can not be overridden.
 * Abstract methods have to be overridden.
 * Normal methods can be overridden.
 */
public interface CosmeticMenu {

    CustomInventory buildInventory(CrownUser user);

    CustomInventory getCustomInv();

    TextComponent getTitle();

    int getSize();

    static void open(CosmeticMenu menu, CrownUser user) {
        user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        user.getPlayer().openInventory(menu.getCustomInv().getInventory());
    }
}
