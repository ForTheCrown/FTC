package net.forthecrown.core.npc;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public final class Npcs {
  private Npcs() {}

  /**
   * Registry of all intractable NPCs
   */
  public static final Registry<SimpleNpc> REGISTRY = Registries.newRegistry();

  public static final NamespacedKey KEY = Keys.forthecrown("interactable_npc");

  public static boolean interact(String id,
                                 Entity entity,
                                 Player player,
                                 boolean cancelled
  ) {
    var npc = REGISTRY.get(id);

    if (npc.isEmpty()) {
      Loggers.getLogger().warn("Unknown NPC key: " + id);
      return cancelled;
    }

    try {
      return npc.get().run(player, entity);
    } catch (CommandSyntaxException e) {
      Exceptions.handleSyntaxException(player, e);
      return cancelled;
    }
  }

  public static void make(String key, Entity entity)
      throws IllegalArgumentException
  {
    Preconditions.checkArgument(
        entity.getType() != EntityType.PLAYER,
        "Players cannot be made into NPCs"
    );

    entity.getPersistentDataContainer()
        .set(KEY, PersistentDataType.STRING, key);
  }
}