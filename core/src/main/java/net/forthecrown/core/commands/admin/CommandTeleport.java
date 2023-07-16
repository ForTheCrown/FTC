package net.forthecrown.core.commands.admin;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.EntitySelector;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

@CommandData("file = 'commands/tp.gcn'")
public class CommandTeleport {

  public static final String YAW_ARG = "yaw";
  public static final String PITCH_ARG = "pitch";

  @VariableInitializer
  private void initVars(Map<String, Object> variables) {
    variables.put("yaw", FloatArgumentType.floatArg(-180, 180));
    variables.put("pitch", FloatArgumentType.floatArg(-90, 90));
  }

  public Location selectorToDestination(CommandSource source, EntitySelector selector)
      throws CommandSyntaxException
  {
    return selector.findEntity(source).getLocation();
  }

  public void teleportSelf(
      CommandSource source,
      @Argument("location") Location dest,
      @Argument(value = YAW_ARG, optional = true) Float yaw,
      @Argument(value = PITCH_ARG, optional = true) Float pitch
  ) throws CommandSyntaxException {
    applyRotations(dest, yaw, pitch);
    teleportSource(source, dest);

    source.sendSuccess(
        Text.format("Teleporting to &e{0, location, -c -w}&r.",
            NamedTextColor.GRAY,
            dest
        ),

        // Don't announce
        false
    );
  }

  public void entitiesToLocation(
      CommandSource source,
      @Argument("from_entity") List<Entity> entities,
      @Argument(value = "dest_entity", optional = true) Location destLocation,
      @Argument(value = "location", optional = true) Location destEntity,
      @Argument(value = YAW_ARG, optional = true) Float yaw,
      @Argument(value = PITCH_ARG, optional = true) Float pitch,
      @Argument(value = "facing", optional = true) Location facing
  ) throws CommandSyntaxException {
    Location dest = destLocation == null ? destEntity : destLocation;
    Objects.requireNonNull(dest);

    if (entities.isEmpty()) {
      throw Grenadier.exceptions().noEntityFound();
    }

    if (facing == null) {
      applyRotations(dest, yaw, pitch);
    } else {
      Vector direction = facing.toVector().subtract(dest.toVector());
      dest.setDirection(direction);
    }

    int successes = 0;
    int failed = 0;

    for (Entity e: entities) {
      if (e instanceof Player player) {
        User user = Users.get(player);

        user.createTeleport(dest::clone, Type.TELEPORT)
            .setDelay(null)
            .start();

        successes++;
      } else {
        boolean success = teleportTo(e, dest);

        if (success) {
          successes++;
        } else {
          failed++;
        }
      }
    }

    if (successes == 0 && failed >= 1) {
      throw Exceptions.create("Failed to teleport any entities");
    }

    if (failed >= 1) {
      source.sendMessage(
          Text.format("Failed to teleport &e{0, number}&r entities",
              NamedTextColor.GRAY,
              failed
          )
      );
    }

    if (successes == 1) {
      var only = entities.get(0);

      Component displayName = only instanceof Player player
          ? Users.get(player).displayName()
          : only.teamDisplayName();

      source.sendSuccess(
          Text.format("Teleported &e{0}&r to &6{1, location, -c -w}&r.",
              NamedTextColor.GRAY, displayName, dest
          )
      );
    } else {
      source.sendSuccess(
          Text.format(
              "Teleported &e{0, number}&r entities to &6{1, location, -c -w}&r.",
              NamedTextColor.GRAY, successes, dest
          )
      );
    }
  }

  public void teleportToEntity(
      CommandContext<CommandSource> context,
      CommandSource source,
      @Argument("from_entity") List<Entity> entities
  ) throws CommandSyntaxException {
    if (entities.isEmpty()) {
      throw Grenadier.exceptions().noEntityFound();
    }

    if (entities.size() > 1) {
      var range = CommandContexts.getNodeRange(context, "from_entity");
      var reader = Readers.create(context.getInput(), range.getStart());

      throw Grenadier.exceptions().selectorOnlyOneEntity(reader);
    }

    Entity target = entities.get(0);
    Location location = target.getLocation();

    teleportSource(source, location);

    Component displayName = target instanceof Player player
        ? Users.get(player).displayName()
        : target.teamDisplayName();

    source.sendSuccess(
        Text.format("Teleported to &e{0}&r.",
            NamedTextColor.GRAY,
            displayName
        ),

        // Don't announce
        false
    );
  }

  private void teleportSource(CommandSource source, Location location)
      throws CommandSyntaxException
  {
    if (source.isPlayer()) {
      var user = Users.get(source.asPlayer());

      user.createTeleport(location::clone, Type.TELEPORT)
          .setDelay(null)
          .start();
    } else {
      var entity = source.asEntity();
      boolean success = teleportTo(entity, location);

      if (!success) {
        throw Exceptions.create("Failed to teleport");
      }
    }
  }

  private boolean teleportTo(Entity e, Location l) {
    return e.teleport(l.clone(),
        TeleportCause.COMMAND,
        EntityState.RETAIN_PASSENGERS
    );
  }

  private void applyRotations(Location l, Float yaw, Float pitch) {
    if (yaw != null) {
      l.setYaw(yaw);
    }

    if (pitch != null) {
      l.setPitch(pitch);
    }
  }
}
