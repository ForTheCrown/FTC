package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.DungeonLevels;
import net.forthecrown.dungeons.level.SpawnerView;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class CommandFindSpawners extends FtcCommand {

    public CommandFindSpawners() {
        super("FindSpawners");

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /FindSpawners
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();
                    DungeonLevel level = DungeonLevels.find(player.getLocation());

                    if(level == null) {
                        throw FtcExceptionProvider.translatable("dungeons.notInLevel");
                    }

                    return view(player, level);
                })

                .then(argument("key", Keys.argumentType())
                        .executes(c -> {
                            Key key = c.getArgument("key", NamespacedKey.class);
                            DungeonLevel level = Registries.DUNGEON_LEVELS.get(key);

                            if(level == null) {
                                throw FtcExceptionProvider.translatable("dungeons.unknownLevel", Component.text(key.asString()));
                            }

                            return view(c.getSource().asPlayer(), level);
                        })
                );
    }

    private int view(Player player, DungeonLevel level) throws CommandSyntaxException {
        SpawnerView view = level.view(player);

        if(view == null) {
            throw FtcExceptionProvider.translatable("dungeons.levelIsClear");
        }

        player.sendMessage(
                Component.translatable("dungeons.viewing", NamedTextColor.YELLOW)
        );
        return 0;
    }
}