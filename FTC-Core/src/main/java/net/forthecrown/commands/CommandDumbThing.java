package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.CrownRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.function.Function;

public class CommandDumbThing extends FtcCommand {
    private static final CrownRandom RANDOM = new CrownRandom();

    private final Function<Player, Integer> func;

    public CommandDumbThing(String name, Permission permission, String description, Function<Player, Integer> func){
        super(name, Crown.inst());

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

    public static void init(){
        new CommandDumbThing("beezooka",
                Permissions.DONATOR_3,
                "Shoots a bee :D",
                player -> {
                    Location l = player.getEyeLocation();

                    Bee bee = l.getWorld().spawn(l, Bee.class);
                    bee.setVelocity(player.getEyeLocation().getDirection().multiply(2));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
                        final Location loc = bee.getLocation();
                        bee.remove();
                        loc.getWorld().createExplosion(loc, 0F);
                    }, 20);
                    return 0;
                }
        );

        new CommandDumbThing("kittycannon",
                Permissions.DONATOR_3,
                "Shoots a kitten at people",
                player -> {
                    Location l = player.getEyeLocation();

                    Cat cat = l.getWorld().spawn(l, Cat.class);
                    cat.setBaby();
                    cat.setTamed(true);
                    cat.setCatType(Cat.Type.values()[RANDOM.intInRange(0, Cat.Type.values().length-1)]);

                    cat.setVelocity(player.getEyeLocation().getDirection().multiply(2));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Crown.inst(), () -> {
                        final Location loc = cat.getLocation();
                        cat.remove();
                        loc.getWorld().createExplosion(loc, 0F);
                    }, 20);
                    return 0;
                }
        );
    }
}