package net.forthecrown.commands.admin;

import static net.kyori.adventure.text.Component.text;

import java.util.Collection;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class CommandNPC extends FtcCommand {

  public CommandNPC() {
    super("npc");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   *
   * Permissions used:
   *
   * Main Author: Julie
   */

  @Override
  @SuppressWarnings("unchecked")
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("entities", ArgumentTypes.entities())
            .then(argument("key", RegistryArguments.NPC)

                .executes(c -> {
                  Collection<Entity> entities = ArgumentTypes.getEntities(c, "entities");
                  Holder<SimpleNpc> npcHolder = c.getArgument("key", Holder.class);
                  String key = npcHolder.getKey();

                  entities.forEach(e -> {
                    if (e.getType() == EntityType.PLAYER) {
                      return;
                    }

                    Npcs.make(key, e);
                  });

                  c.getSource().sendSuccess(
                      text("Added " + key + " tag to " + entities.size() + " entities")
                  );
                  return 0;
                })
            )
        );
  }
}