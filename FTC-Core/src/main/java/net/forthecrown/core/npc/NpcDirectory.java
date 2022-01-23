package net.forthecrown.core.npc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class NpcDirectory {
    private NpcDirectory() {}

    public static final NamespacedKey KEY = new NamespacedKey(Crown.inst(), "interactable_npc");

    public static void interact(String id, Entity entity, Player player) {
        Key key = Keys.parse(id);

        InteractableNPC npc = Registries.NPCS.get(key);
        if(npc == null ) {
            Crown.logger().warn("Unknown NPC key: " + key.asString());
            return;
        }

        try {
            npc.run(player, entity);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(player, e);
        }
    }

    public static void make(Key key, Entity entity) throws IllegalArgumentException {
        Validate.isTrue(entity.getType() != EntityType.PLAYER, "Entity cannot be player");
        Validate.isTrue(Registries.NPCS.contains(key), "No NPC with key " + key + " exists in registry");

        entity.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, key.asString());
    }

    public static boolean isNPC(Entity entity) {
        return entity.getPersistentDataContainer().has(KEY, PersistentDataType.STRING);
    }
}
