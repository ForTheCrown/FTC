package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.npc.NpcDirectory;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.registry.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

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
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("entities", EntityArgument.multipleEntities())
                        .then(argument("key", FtcCommands.ftcKeyType())
                                .suggests((c, b) -> FtcSuggestionProvider.suggestRegistry(b, Registries.NPCS))

                                .executes(c -> {
                                    Collection<Entity> entities = EntityArgument.getEntities(c, "entities");
                                    NamespacedKey key = KeyArgument.getKey(c, "key");

                                    if(!Registries.NPCS.contains(key)) throw FtcExceptionProvider.create("NPC registry does not contain this key");

                                    entities.forEach(e -> {
                                        if(e.getType() == EntityType.PLAYER) return;
                                        e.getPersistentDataContainer().set(NpcDirectory.KEY, PersistentDataType.STRING, key.asString());
                                    });

                                    c.getSource().sendAdmin("Added " + key.asString() + " tag to " + entities.size() + " entities");
                                    return 0;
                                })
                        )
                );
    }
}