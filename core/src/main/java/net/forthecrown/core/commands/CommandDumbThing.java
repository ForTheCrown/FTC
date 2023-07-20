package net.forthecrown.core.commands;

import java.util.Random;
import java.util.function.Function;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class CommandDumbThing extends FtcCommand {

  private final Function<Player, Integer> func;

  public CommandDumbThing(String name, Permission permission, String description,
                          Function<Player, Integer> func
  ) {
    super(name);

    this.func = func;

    setPermission(permission);
    setDescription(description);
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          Player player = c.getSource().asPlayer();

          return func.apply(player);
        });
  }

  public static void createCommands() {
    new CommandDumbThing("beezooka",
        CorePermissions.CMD_BEEZOOKA,
        "Shoots a bee :D",
        player -> {
          Location l = player.getEyeLocation();

          Bee bee = l.getWorld().spawn(l, Bee.class);
          bee.setVelocity(player.getEyeLocation().getDirection().multiply(2));

          Tasks.runLater(() -> {
            final Location loc = bee.getLocation();
            bee.remove();
            loc.getWorld().createExplosion(loc, 0F);
          }, 20);
          return 0;
        }
    );

    new CommandDumbThing("kittycannon",
        CorePermissions.CMD_KITTY_CANNON,
        "Shoots a kitten at people",

        new Function<>() {
          final Random random = new Random();

          @Override
          public Integer apply(Player player) {
            Location l = player.getEyeLocation();

            Cat cat = l.getWorld().spawn(l, Cat.class);
            cat.setBaby();
            cat.setTamed(true);
            cat.setCatType(Cat.Type.values()[random.nextInt(Cat.Type.values().length)]);

            cat.setVelocity(player.getEyeLocation().getDirection().multiply(2));

            Tasks.runLater(() -> {
              final Location loc = cat.getLocation();
              cat.remove();
              loc.getWorld().createExplosion(loc, 0F);
            }, 20);
            return 0;
          }
        }
    );
  }
}