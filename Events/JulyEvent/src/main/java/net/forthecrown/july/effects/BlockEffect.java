package net.forthecrown.july.effects;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface BlockEffect {
    void apply(Player player);
    Material getMaterial();
}
