package net.forthecrown.core.npc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface InteractableNPC {
    void run(Player player, Entity entity) throws CommandSyntaxException;
}