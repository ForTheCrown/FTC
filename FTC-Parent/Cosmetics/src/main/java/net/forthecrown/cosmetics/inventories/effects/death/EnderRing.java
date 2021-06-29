package net.forthecrown.cosmetics.inventories.effects.death;

import net.forthecrown.core.inventory.CrownItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class EnderRing extends CosmeticDeathEffect {
    @Override
    public String getEffectName() {
        return "ENDER_RING";
    }

    @Override
    public void activateEffect(Location loc) {
        //loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1f, 0.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.5f, 1f);
        double y2 = loc.getY();
        for (int i = 0; i < 3; i++) {
            loc.setY(y2+i);
            for (int j = 0; j < 5; j++) {
                loc.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 1);
            }
        }
    }

    @Override
    public ItemStack getEffectItem() {
        return CrownItems.makeItem(Material.GRAY_DYE, 1, true,
                "&eEnder Ring",
                ChatColor.GRAY + "Ender particles doing ring stuff.",
                "",
                ChatColor.GRAY + "Makes you scream like an enderman lol",
                "",
                ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "2000" + ChatColor.GRAY + " gems.");
    }
}
