package net.forthecrown.packet;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface EntityRenderer {

  boolean test(Player player);

  Component render(Player player);
}
