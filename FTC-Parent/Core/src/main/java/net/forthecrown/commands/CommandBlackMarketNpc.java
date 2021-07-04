package net.forthecrown.commands;

import net.forthecrown.commands.arguments.KeyType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.pirates.Pirates;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class CommandBlackMarketNpc extends FtcCommand {

    public CommandBlackMarketNpc() {
        super("bm_npc");

        setPermission(Permissions.CORE_ADMIN);
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
                .then(argument("target", EntityArgument.entity())
                        .then(argument("key", KeyType.key("ftccore"))
                                .suggests((c, b) -> CompletionProvider.suggestKeys(b, Pirates.getPirateEconomy().getNpcRegistry().getKeys()))

                                .executes(c -> {
                                    Entity entity = EntityArgument.getEntity(c, "target");
                                    Key key = c.getArgument("key", Key.class);

                                    entity.getPersistentDataContainer().set(Pirates.BM_MERCHANT, PersistentDataType.STRING, key.asString());

                                    c.getSource().sendAdmin("Added BM NPC tag");
                                    return 0;
                                })
                        )
                );
    }
}