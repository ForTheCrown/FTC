package net.forthecrown.cosmetics.inventories.effects.death;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Explosion extends CosmeticDeathEffect {
    @Override
    public String getEffectName() {
        return "EXPLOSION";
    }

    @Override
    public void activateEffect(Location loc) {
        loc.getWorld().playEffect(loc, Effect.END_GATEWAY_SPAWN, 1);
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eCreeper",
                ChatColor.GRAY + "Always wanted to know what it feels like...",
                "",
                ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
    }
}
