package net.forthecrown.inventory.weapon;

import net.forthecrown.inventory.weapon.click.ClickHistory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class WeaponContext {
    public final Player player;
    public final ItemStack item;
    public final RoyalSword sword;
    public final ClickHistory clickHistory;

    public WeaponContext(Player player, ItemStack item, RoyalSword sword, ClickHistory history) {
        this.player = player;
        this.item = item;
        this.sword = sword;
        clickHistory = history;
    }
}
