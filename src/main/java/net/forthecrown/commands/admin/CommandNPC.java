package net.forthecrown.commands.admin;

import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collection;

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
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("entities", EntityArgument.multipleEntities())
                        .then(argument("key", RegistryArguments.NPC)

                                .executes(c -> {
                                    Collection<Entity> entities = EntityArgument.getEntities(c, "entities");
                                    Holder<SimpleNpc> npcHolder = c.getArgument("key", Holder.class);
                                    String key = npcHolder.getKey();

                                    entities.forEach(e -> {
                                        if (e.getType() == EntityType.PLAYER) {
                                            return;
                                        }

                                        Npcs.make(key, e);
                                    });

                                    c.getSource().sendAdmin("Added " + key + " tag to " + entities.size() + " entities");
                                    return 0;
                                })
                        )
                );
    }
}