package net.forthecrown.core.npc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class NpcDirectory {
    private NpcDirectory() {}

    public static final NamespacedKey KEY = new NamespacedKey(Crown.inst(), "interactable_npc");

    public static void interact(String id, Entity entity, Player player) {
        Key key = FtcUtils.parseKey(id);

        InteractableNPC npc = Registries.NPCS.get(key);
        if(npc == null ) {
            Crown.logger().warning("Unknown NPC key: " + key.asString());
            return;
        }

        try {
            npc.run(player, entity);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(player, e);
        }
    }
}
