package net.forthecrown.packet;

import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface EntityRenderer {

  boolean test(Player player, int entityId, @Nullable Component existingName);

  Component render(Player player, int entityId, @Nullable Component existingName);
}
