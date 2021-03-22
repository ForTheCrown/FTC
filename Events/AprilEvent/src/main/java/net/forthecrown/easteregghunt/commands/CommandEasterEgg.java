package net.forthecrown.easteregghunt.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.forthecrown.easteregghunt.EasterMain;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandEasterEgg extends CrownCommandBuilder {

    public CommandEasterEgg(){
        super("easterevent", EasterMain.instance);

        setPermission("ftc.easter.admin");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                /*.then(argument("updatelb")
                        .executes(c -> {
                            EasterMain.leaderboard.update();
                            broadcastAdmin(c.getSource(), "Event leaderboard updated");
                            return 0;
                        })
                )*/
                .then(argument("spawns")
                        .then(argument("add").then(argument("location", Vector3DType.vec3(true))
                                .executes(c -> {
                                    Location location = Vector3DType.getLocation(c, "location");
                                    EasterMain.eggSpawns.add(location);
                                    broadcastAdmin(c.getSource(), "Location added");
                                    return 0;
                                })
                        ))
                        .then(argument("remove").then(argument("location", Vector3DType.vec3(true))
                                .executes(c -> {
                                    Location location = Vector3DType.getLocation(c, "location");
                                    EasterMain.eggSpawns.remove(location);
                                    broadcastAdmin(c.getSource(), "Location removed");
                                    return 0;
                                })
                        ))
                        .then(argument("list")
                                .executes(c -> {
                                    c.getSource().getBukkitSender().sendMessage(EasterMain.eggSpawns.toString());
                                    return 0;
                                })
                        )
                )
                .then(argument("debug")
                        .then(argument("startSpawning")
                                .executes(c -> {
                                    EasterMain.spawner.placeEggs();
                                    broadcastAdmin(c.getSource(), "Beginning placement of eggs");
                                    return 0;
                                })
                        )
                        .then(argument("placerandom")
                                .executes(c -> {
                                    EasterMain.spawner.placeRandomEgg();
                                    broadcastAdmin(c.getSource(), "Placing random egg");
                                    return 0;
                                })
                        )
                        .then(argument("enter")
                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    EasterMain.event.start(player);
                                    broadcastAdmin(c.getSource(), "Starting event");
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
