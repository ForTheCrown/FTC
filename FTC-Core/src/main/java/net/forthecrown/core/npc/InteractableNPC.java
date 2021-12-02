package net.forthecrown.core.npc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface InteractableNPC {
    void run(Player player, Entity entity) throws CommandSyntaxException;
}
