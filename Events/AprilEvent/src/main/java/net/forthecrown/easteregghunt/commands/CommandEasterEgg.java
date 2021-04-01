package net.forthecrown.easteregghunt.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.forthecrown.easteregghunt.EasterMain;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandEasterEgg extends CrownCommandBuilder {

    public CommandEasterEgg(){
        super("easterevent", EasterMain.inst);

        setPermission("ftc.easter.admin");
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("spawns")
                        .then(argument("location", Vector3DType.vec3())
                                .then(argument("add")
                                        .executes(c -> {
                                            Location location = Vector3DType.getLocation(c, "location");
                                            EasterMain.eggSpawns.add(location);
                                            broadcastAdmin(c.getSource(), "Location added");
                                            return 0;
                                        })
                                )
                                .then(argument("remove")
                                        .executes(c -> {
                                            Location location = Vector3DType.getLocation(c, "location");
                                            EasterMain.eggSpawns.remove(location);
                                            broadcastAdmin(c.getSource(), "Location removed");
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("list")
                                .executes(c -> {
                                    c.getSource().getBukkitSender().sendMessage(EasterMain.eggSpawns.toString());
                                    return 0;
                                })
                        )
                )

                .then(argument("updatelb")
                        .executes(c -> {
                            EasterMain.leaderboard.update();
                            broadcastAdmin(c.getSource(), "Updating leaderboard");
                            return 0;
                        })
                )

                .then(argument("debug")
                        .then(argument("spawnBunny")
                                .executes(c -> {
                                    EasterMain.bunny.spawn();
                                    broadcastAdmin(c.getSource(), "Spawning CrazyBunny");
                                    return 0;
                                })
                        )

                        .then(argument("startSpawning")
                                .executes(c -> {
                                    EasterMain.spawner.placeEggs();
                                    broadcastAdmin(c.getSource(), "Beginning placement of eggs");
                                    return 0;
                                })
                        )
                        .then(argument("placerandom")
                                .executes(c -> {
                                    EasterMain.spawner.placeRandomEgg(null);
                                    broadcastAdmin(c.getSource(), "Placing random egg");
                                    return 0;
                                })
                        )
                        .then(argument("clearList")
                                .executes(c -> {
                                    EasterMain.tracker().clear();
                                    broadcastAdmin(c.getSource(), "cleared list");
                                    return 0;
                                })
                        )
                        .then(argument("start")
                                .executes(c -> {
                                    try {
                                        Player player = getPlayerSender(c);
                                        EasterMain.event.start(player);
                                        broadcastAdmin(c.getSource(), "Starting event");
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                        .then(argument("end")
                                .executes(c -> {
                                    testPlayerSender(c.getSource());
                                    EasterMain.event.end(EasterMain.event.entry);
                                    broadcastAdmin(c.getSource(), "Ending event");
                                    return 0;
                                })
                        )
                );
    }
}