package net.forthecrown.waypoints.command;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.util.Tick;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.ParseResult;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.WaypointProperty;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.WaypointsPlugin;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.forthecrown.waypoints.util.DelayedWaypointIterator;
import net.forthecrown.waypoints.util.WaypointAction;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.math.vector.Vector3i;

public class CommandWaypoints extends FtcCommand {

  public CommandWaypoints() {
    super("Waypoints");

    setPermission(WPermissions.WAYPOINTS_ADMIN);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /Waypoints
   *
   * Permissions used:
   *
   * Main Author:
   */

  private Waypoint get(CommandContext<CommandSource> c) throws CommandSyntaxException {
    ParseResult<Waypoint> result = c.getArgument("waypoint", ParseResult.class);
    return result.get(c.getSource(), false);
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("save", "Saves the waypoints plugin");
    factory.usage("reload", "Reloads the waypoints plugin");

    var prefixed = factory.withPrefix("<waypoint>");

    prefixed.usage("move [<pos: x, y, z>]")
        .addInfo("Moves a waypoint. If [pos] is not set, then")
        .addInfo("the waypoint is moved to where you're standing");

    prefixed.usage("data")
        .addInfo("Shows a waypoint's region raw NBT data");

    prefixed.usage("remove")
        .addInfo("Deletes <waypoint>");

    prefixed = prefixed.withPrefix("property <property name>");

    prefixed.usage("")
        .addInfo("Shows the <property name>'s value");

    prefixed.usage("<value>")
        .addInfo("Sets the <property name>'s value to <value>");

    prefixed.usage("-clear")
        .addInfo("Clears the <property name>'s value");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command

        .then(literal("save")
            .executes(context -> {
              WaypointManager.getInstance().save();

              context.getSource().sendSuccess(text("Saved Waypoints plugin"));
              return 0;
            })
        )

        .then(literal("reload")
            .executes(context -> {
              WaypointsPlugin plugin = JavaPlugin.getPlugin(WaypointsPlugin.class);
              plugin.reloadConfig();
              WaypointManager.getInstance().load();

              context.getSource().sendSuccess(text("Reloaded waypoints plugin"));
              return 0;
            })
        )

        .then(literal("update-all").executes(this::updateAll))

        .then(argument("waypoint", WaypointCommands.WAYPOINT)
            // /waypoints <point> move
            .then(literal("move")
                .executes(c -> {
                  var player = c.getSource().asPlayer();
                  var loc = player.getLocation();

                  Vector3i pos = Vectors.intFrom(loc);

                  return move(c, pos, loc.getWorld());
                })

                // /waypoints <point> move <pos>
                .then(argument("pos", ArgumentTypes.blockPosition())
                    .executes(c -> {
                      var loc = ArgumentTypes.getLocation(c, "pos");
                      Vector3i pos = Vectors.intFrom(loc);

                      return move(c, pos, loc.getWorld());
                    })
                )
            )

            // /waypoints <point> data
            .then(literal("data")
                .executes(c -> {
                  var waypoint = get(c);
                  CompoundTag tag = BinaryTags.compoundTag();
                  waypoint.save(tag);

                  c.getSource().sendMessage(Text.displayTag(tag, true));
                  return 0;
                })
            )

            .then(literal("remove")
                .executes(c -> {
                  var waypoint = get(c);
                  WaypointManager.getInstance()
                      .removeWaypoint(waypoint);

                  c.getSource().sendSuccess(text("Removed waypoint"));
                  return 0;
                })
            )

            .then(literal("property")

                // /waypoints <point> property <prop>
                .then(argument("property", WaypointCommands.PROPERTY)
                    .executes(c -> {
                      var waypoint = get(c);
                      Holder<WaypointProperty> holder
                          = (Holder) c.getArgument("property", Holder.class);

                      WaypointProperty<?> property = holder.getValue();

                      var value = waypoint.get(property);

                      if (value == null) {
                        c.getSource().sendMessage(
                            Text.format("Property {0} is unset", property.getName())
                        );
                        return 0;
                      }

                      c.getSource().sendMessage(
                          Text.format("Property {0}: {1}",
                              property.getName(), value
                          )
                      );
                      return 0;
                    })

                    // /waypoints <point> property <prop> <value>
                    .then(argument("value", StringArgumentType.greedyString())
                        .suggests((context, builder) -> {
                          Holder<WaypointProperty> holder = (Holder) context.getArgument("property",
                              Holder.class);
                          var property = holder.getValue();

                          return property.getParser().listSuggestions(context, builder);
                        })

                        .executes(c -> {
                          return property(c, false);
                        })
                    )

                    .then(literal("-clear")
                        .executes(c -> property(c, true))
                    )
                )
            )
        );
  }

  private int updateAll(CommandContext<CommandSource> c) {
    WaypointManager manager = WaypointManager.getInstance();

    DelayedWaypointIterator it = new DelayedWaypointIterator(
        manager.getWaypoints().iterator(),
        Tick.of(1),
        new WaypointUpdateAction(c.getSource())
    );

    it.schedule();
    return 0;
  }

  private int property(CommandContext<CommandSource> c, boolean unset)
      throws CommandSyntaxException
  {
    Waypoint waypoint = get(c);
    Holder<WaypointProperty> holder = (Holder) c.getArgument("property", Holder.class);
    var property = holder.getValue();
    var type = property.getParser();

    if (unset) {
      waypoint.set(property, null);

      c.getSource().sendSuccess(
          Text.format("Unset property {0} for waypoint",
              holder.getKey(),
              waypoint.getEffectiveName()
          )
      );
      return 0;
    }

    String strInput = c.getArgument("value", String.class);
    StringReader input = new StringReader(strInput);

    Object value = type.parse(input);
    Commands.ensureCannotRead(input);

    property.validateValue(waypoint, value);
    waypoint.set(property, value);

    c.getSource().sendSuccess(
        Text.format("Value of {0} is now {1}",
            property.getName(), value
        )
    );
    return 0;
  }

  private int move(CommandContext<CommandSource> c, Vector3i pos, World world)
      throws CommandSyntaxException
  {
    Waypoint waypoint = get(c);

    if (waypoint.getPosition().equals(pos) && waypoint.getWorld().equals(world)) {
      throw Exceptions.format("Waypoint is already located at world={0}, pos={1}",
          world.getName(), pos
      );
    }

    waypoint.setPosition(pos, world);

    c.getSource().sendSuccess(
        Text.format("Moved waypoint to world={0}, pos={1}",
            world.getName(), pos
        )
    );
    return 0;
  }

  private static class WaypointUpdateAction implements WaypointAction {

    final CommandSource source;
    int updated = 0;

    public WaypointUpdateAction(CommandSource source) {
      this.source = source;
    }

    @Override
    public void accept(Waypoint waypoint) {
      if (waypoint.getType() == WaypointTypes.ADMIN) {
        return;
      }

      var platform = waypoint.getPlatform();
      if (platform != null) {
        Waypoints.placePlatform(waypoint.getWorld(), platform);
      }

      if (waypoint.getType() == WaypointTypes.REGION_POLE) {
        var pos = waypoint.getPosition();
        var world = waypoint.getWorld();

        var block = Vectors.getBlock(pos, world);

        clearColumnUp(block.getRelative( 1, 1,  0));
        clearColumnUp(block.getRelative(-1, 1,  0));
        clearColumnUp(block.getRelative( 0, 1,  1));
        clearColumnUp(block.getRelative( 0, 1, -1));
      }

      waypoint.setInfoSigns(true);
      waypoint.updateOutline();
      waypoint.updateResidentsSign();
      waypoint.updateNameSign();
      waypoint.setLightBlock(true);
      waypoint.setEditSign(true);

      waypoint.set(WaypointProperties.INVULNERABLE, true);

      updated++;
    }

    private void clearColumnUp(Block block) {
      for (int i = 0; i < 3; i++) {
        Block b = block.getRelative(0, i, 0);
        b.setType(Material.AIR, false);
      }
    }

    @Override
    public void onFinish() {
      source.sendSuccess(Text.format("Updated {0, number} waypoints", updated));
    }
  }
}