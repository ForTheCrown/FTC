package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.function.Function;

public class CommandDumbThing extends FtcCommand {
    private final Function<Player, Integer> func;

    public CommandDumbThing(String name, Permission permission, String description, Function<Player, Integer> func){
        super(name);

        this.func = func;
        this.permission = permission;
        this.description = description;

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();

                    return func.apply(player);
                });
    }

    public static void createCommands(){
        new CommandDumbThing("beezooka",
                Permissions.CMD_BEEZOOKA,
                "Shoots a bee :D",
                player -> {
                    Location l = player.getEyeLocation();

                    Bee bee = l.getWorld().spawn(l, Bee.class);
                    bee.setVelocity(player.getEyeLocation().getDirection().multiply(2));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.plugin(), () -> {
                        final Location loc = bee.getLocation();
                        bee.remove();
                        loc.getWorld().createExplosion(loc, 0F);
                    }, 20);
                    return 0;
                }
        );

        new CommandDumbThing("kittycannon",
                Permissions.CMD_KITTY_CANNON,
                "Shoots a kitten at people",
                player -> {
                    Location l = player.getEyeLocation();

                    Cat cat = l.getWorld().spawn(l, Cat.class);
                    cat.setBaby();
                    cat.setTamed(true);
                    cat.setCatType(Cat.Type.values()[Util.RANDOM.nextInt(Cat.Type.values().length)]);

                    cat.setVelocity(player.getEyeLocation().getDirection().multiply(2));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.plugin(), () -> {
                        final Location loc = cat.getLocation();
                        cat.remove();
                        loc.getWorld().createExplosion(loc, 0F);
                    }, 20);
                    return 0;
                }
        );
    }
}