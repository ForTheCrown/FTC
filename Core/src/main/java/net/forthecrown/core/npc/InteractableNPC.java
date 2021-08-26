package net.forthecrown.core.npc;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface InteractableNPC {
    void run(Player player, Entity entity) throws RoyalCommandException;
}
