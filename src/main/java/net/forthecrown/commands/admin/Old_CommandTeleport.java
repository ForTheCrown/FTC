package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3d;

public class Old_CommandTeleport extends FtcCommand {

  public Old_CommandTeleport() {
    super("fteleport");

    setPermission(Permissions.CMD_TELEPORT);
    setAliases("tp", "teleport", "eteleport", "etp");
    setDescription("FTC's version of the vanilla /tp command");

    register();
  }

  @Override
  public String getHelpListName() {
    return "teleport";
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("entity", ArgumentTypes.entities())
            .then(argument("entity_to", ArgumentTypes.player())
                .executes(c -> {
                  Entity entity = ArgumentTypes.getPlayer(c, "entity_to");
                  Collection<Entity> entities = ArgumentTypes.getEntities(c, "entity");

                  Component display = entity.teamDisplayName();

                  if (entity.getType() == EntityType.PLAYER) {
                    display = Users.get(entity.getUniqueId())
                        .displayName();
                  }

                  return teleport(entities, entity.getLocation(), display, c.getSource());
                })
            )

            .then(argument("location_to", ArgumentTypes.position())
                .executes(c -> entityTeleport(c, false, false))

                .then(argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                    .executes(c -> entityTeleport(c, true, false))

                    .then(argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                        .executes(c -> entityTeleport(c, true, true))
                    )
                )

                .then(literal("facing")
                    .then(argument("facing_pos", ArgumentTypes.position())
                        .executes(
                            c -> teleportFacing(c, ArgumentTypes.getLocation(c, "facing_pos")))
                    )

                    .then(literal("facingEntity")
                        .then(argument("facing_entity", ArgumentTypes.entity())
                            .executes(c -> teleportFacing(c,
                                ArgumentTypes.getEntity(c, "facing_entity").getLocation()))
                        )
                    )
                )
            )
        )

        .then(argument("entity", ArgumentTypes.entity())
            .executes(c -> {
              User user = getUserSender(c);
              Entity entity = ArgumentTypes.getEntity(c, "entity");

              Component display = entity.teamDisplayName();

              if (entity instanceof Player) {
                display = Users.get(entity.getUniqueId())
                    .displayName();
              }

              if (!user.checkTeleporting()) {
                return 0;
              }

              user.createTeleport(entity::getLocation, UserTeleport.Type.TELEPORT)
                  .setAsync(false)
                  .setDelayed(false)
                  .setSilent(true)
                  .start();

              c.getSource().sendSuccess(
                  Component.text("Teleported ")
                      .append(user.displayName().color(NamedTextColor.YELLOW))
                      .append(Component.text(" to "))
                      .append(display.color(NamedTextColor.YELLOW))
              );
              return 0;
            })
        )

        .then(argument("location", ArgumentTypes.position())
            .executes(c -> teleport(c, false, false))

            .then(argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                .executes(c -> teleport(c, true, false))

                .then(argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                    .executes(c -> teleport(c, true, true))
                )
            )
        );
  }

  private int teleportFacing(CommandContext<CommandSource> c, Location facing)
      throws CommandSyntaxException {
    Location location = ArgumentTypes.getLocation(c, "location_to");
    Vector3d dif = Vectors.doubleFrom(location.clone().subtract(facing));

    location.setYaw((float) Vectors.getYaw(dif));
    location.setPitch((float) Vectors.getPitch(dif));

    Collection<Entity> entities = ArgumentTypes.getEntities(c, "entity");

    return teleport(entities, location, Text.clickableLocation(location, false), c.getSource());
  }

  private int entityTeleport(CommandContext<CommandSource> c, boolean yaw, boolean pitch)
      throws CommandSyntaxException {
    Location location = ArgumentTypes.getLocation(c, "location_to");
    Collection<Entity> entities = ArgumentTypes.getEntities(c, "entity");

    if (yaw) {
      location.setYaw(c.getArgument("yaw", Float.class));
    }

    if (pitch) {
      location.setPitch(c.getArgument("pitch", Float.class));
    }

    return teleport(entities, location, Text.clickableLocation(location, false), c.getSource());
  }

  private int teleport(CommandContext<CommandSource> c, boolean yawGiven, boolean pitchGiven)
      throws CommandSyntaxException {
    User user = getUserSender(c);
    Location loc = ArgumentTypes.getLocation(c, "location");

    if (yawGiven) {
      loc.setYaw(c.getArgument("yaw", Float.class));
    }

    if (pitchGiven) {
      loc.setPitch(c.getArgument("pitch", Float.class));
    }

    if (!user.checkTeleporting()) {
      return 0;
    }

    user.createTeleport(() -> loc, UserTeleport.Type.TELEPORT)
        .setAsync(false)
        .setSilent(true)
        .setDelayed(false)
        .start();

    c.getSource().sendSuccess(
        Component.text("Teleported ")
            .append(user.displayName().color(NamedTextColor.YELLOW))
            .append(Component.text(" to "))
            .append(Text.clickableLocation(loc, false).color(NamedTextColor.YELLOW))
    );
    return 0;
  }

  private int teleport(Collection<Entity> entities, Location location, Component destDisplayName,
                       CommandSource source
  ) throws CommandSyntaxException {
    if (entities.isEmpty()) {
      throw Grenadier.exceptions().noEntityFound();
    }

    int amount = 0;
    for (Entity e : entities) {
      if (e.getType() != EntityType.PLAYER) {
        e.teleport(location);
        amount++;
        continue;
      }

      User user = Users.get(e.getUniqueId());
      if (user.isTeleporting()) {
        continue;
      }

      user.createTeleport(() -> location, UserTeleport.Type.TELEPORT)
          .setSetReturn(true)
          .setDelayed(false)
          .setSilent(true)
          .start();

      amount++;
    }

    Component entMsg =
        entities.size() > 1 ? Component.text(amount + " entities").color(NamedTextColor.YELLOW)
            : entDisplay(entities).color(NamedTextColor.YELLOW);

    source.sendSuccess(
        Component.text("Teleported ")
            .append(entMsg)
            .append(Component.text(" to "))
            .append(destDisplayName.color(NamedTextColor.YELLOW))
    );
    return 0;
  }

  public Component entOrUserDisplayName(Entity entity) {
    if (entity.getType() == EntityType.PLAYER) {
      return Users.get(entity.getUniqueId())
          .displayName();
    }

    return entity.teamDisplayName();
  }

  public Component entDisplay(Collection<Entity> entities) {
    for (Entity entity : entities) {
      return entOrUserDisplayName(entity);
    }

    return null;
  }
}