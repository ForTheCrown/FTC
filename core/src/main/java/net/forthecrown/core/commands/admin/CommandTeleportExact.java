package net.forthecrown.core.commands.admin;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CommandTeleportExact extends FtcCommand {

  public static final ArgumentOption<World> WORLD
      = Options.argument(ArgumentTypes.world(), "world");

  public static final ArgumentOption<Double>
      CORD_X = Options.argument(DoubleArgumentType.doubleArg(), "x"),
      CORD_Y = Options.argument(DoubleArgumentType.doubleArg(), "y"),
      CORD_Z = Options.argument(DoubleArgumentType.doubleArg(), "z");

  public static final ArgumentOption<Float> YAW
      = Options.argument(FloatArgumentType.floatArg(-180, 180))
      .setDefaultValue(0F)
      .addLabel("yaw")
      .build();


  public static final ArgumentOption<Float> PITCH
      = Options.argument(FloatArgumentType.floatArg(-90, 90))
      .setDefaultValue(0F)
      .addLabel("pitch")
      .build();

  public static final OptionsArgument ARGS = OptionsArgument.builder()
      .addRequired(WORLD)

      .addRequired(CORD_X)
      .addRequired(CORD_Y)
      .addRequired(CORD_Z)

      .addOptional(YAW)
      .addOptional(PITCH)

      .build();

  public CommandTeleportExact() {
    super("tp_exact");

    setPermission(CorePermissions.CMD_TELEPORT);
    setDescription("Command for more precise teleportation");

    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage(
        "world=<world> x=<cord> y=<cord> z=<cord> "
            + "[yaw=<value>] [pitch=<value>]"
        )
        .addInfo("Teleports you to the location specified in the parameters");
  }

  public static ClickEvent createLocationClick(Location location) {
    String click = String.format(
        "/tp_exact world=%s x=%s y=%s z=%s pitch=%s yaw=%s",
        location.getWorld().getName(),
        location.getX(),
        location.getY(),
        location.getZ(),
        location.getPitch(),
        location.getYaw()
    );

    return ClickEvent.runCommand(click);
  }

  private static String formatCord(double pos) {
    return String.format("%.02f", pos);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("args", ARGS)
            .executes(c -> {
              var entity = c.getSource().as(Entity.class);
              var args = c.getArgument("args", ParsedOptions.class);

              Location loc = new Location(
                  args.getValue(WORLD),
                  args.getValue(CORD_X),
                  args.getValue(CORD_Y),
                  args.getValue(CORD_Z),
                  args.getValue(YAW),
                  args.getValue(PITCH)
              );

              if (entity instanceof Player player) {
                var user = Users.get(player);

                if (!user.checkTeleporting()) {
                  return 0;
                }

                user.createTeleport(() -> loc, UserTeleport.Type.TELEPORT)
                    .setDelay(null)
                    .setSilent(true)
                    .start();

              } else {
                entity.teleport(loc);
              }

              return 0;
            })
        );
  }
}