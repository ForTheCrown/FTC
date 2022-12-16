package net.forthecrown.core.npc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registries;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class Npcs {
    private Npcs() {}

    public static final NamespacedKey KEY = Keys.forthecrown("interactable_npc");

    public static boolean interact(String id, Entity entity, Player player, boolean cancelled) {
        var npc = Registries.NPCS.get(id);

        if (npc.isEmpty()) {
            FTC.getLogger().warn("Unknown NPC key: " + id);
            return cancelled;
        }

        try {
            return npc.get().run(player, entity);
        } catch (CommandSyntaxException e) {
            Exceptions.handleSyntaxException(player, e);
            return cancelled;
        }
    }

    public static void make(String key, Entity entity) throws IllegalArgumentException {
        Validate.isTrue(entity.getType() != EntityType.PLAYER, "Entity cannot be player");

        entity.getPersistentDataContainer()
                .set(KEY, PersistentDataType.STRING, key);
    }
}