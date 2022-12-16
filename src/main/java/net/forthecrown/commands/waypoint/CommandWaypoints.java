package net.forthecrown.commands.waypoint;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.Readers;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.*;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3i;

public class CommandWaypoints extends FtcCommand {

    public CommandWaypoints() {
        super("Waypoints");

        setPermission(Permissions.WAYPOINTS_ADMIN);
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
        return Arguments.getWaypointNoChecks(c, "waypoint");
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("waypoint", Arguments.WAYPOINT)
                        // /waypoints <point> move
                        .then(literal("move")
                                .executes(c -> {
                                    var player = c.getSource().asPlayer();
                                    var loc = player.getLocation();

                                    Vector3i pos = Vectors.intFrom(loc);

                                    return move(c, pos, loc.getWorld());
                                })

                                // /waypoints <point> move <pos>
                                .then(argument("pos", PositionArgument.blockPos())
                                        .executes(c -> {
                                            var loc = PositionArgument.getLocation(c, "pos");
                                            Vector3i pos = Vectors.intFrom(loc);

                                            return move(c, pos, loc.getWorld());
                                        })
                                )
                        )

                        // /waypoints <point> data
                        .then(literal("data")
                                .executes(c -> {
                                    var waypoint = get(c);
                                    CompoundTag tag = new CompoundTag();
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

                                    c.getSource().sendAdmin("Removed waypoint");
                                    return 0;
                                })
                        )

                        .then(literal("property")

                                // /waypoints <point> property <prop>
                                .then(argument("property", RegistryArguments.WAYPOINT_PROPERTY)
                                        .executes(c -> {
                                            var waypoint = get(c);
                                            Holder<WaypointProperty> holder = (Holder) c.getArgument("property", Holder.class);
                                            var property = holder.getValue();

                                            var value = waypoint.get(property);
                                            var type = property.getSerializer();

                                            if (value == null) {
                                                c.getSource().sendMessage(
                                                        Text.format("Property {0} is unset", property.getName())
                                                );
                                                return 0;
                                            }

                                            c.getSource().sendMessage(
                                                    Text.format("Property {0}: {1}",
                                                            property.getName(), type.display(value)
                                                    )
                                            );
                                            return 0;
                                        })

                                        // /waypoints <point> property <prop> <value>
                                        .then(argument("value", StringArgumentType.greedyString())
                                                .suggests((context, builder) -> {
                                                    Holder<WaypointProperty> holder = (Holder) context.getArgument("property", Holder.class);
                                                    var property = holder.getValue();

                                                    return property.getSerializer()
                                                            .getArgumentType()
                                                            .listSuggestions(context, builder);
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

    private int property(CommandContext<CommandSource> c, boolean unset)
            throws CommandSyntaxException
    {
        Waypoint waypoint = get(c);
        Holder<WaypointProperty> holder = (Holder) c.getArgument("property", Holder.class);
        var property = holder.getValue();

        var type = property.getSerializer();

        if (unset) {
            waypoint.set(property, null);

            c.getSource().sendAdmin(
                    Text.format("Unset property {0}",
                            holder.getKey()
                    )
            );
            return 0;
        }

        String strInput = (String) c.getArgument("value", String.class);
        StringReader input = new StringReader(strInput);

        Object value = type.getArgumentType()
                .parse(input);

        Readers.ensureCannotRead(input);

        if (property == WaypointProperties.NAME) {
            String strValue = value.toString();

            if (!Waypoints.isValidName(strValue)) {
                throw Exceptions.format("'{0}' is an invalid name", strValue);
            }
        }

        waypoint.set(property, value);

        ((CommandSource) c.getSource()).sendAdmin(
                Text.format("Value of {0} is now {1}",
                        property.getName(), type.display(value)
                )
        );
        return 0;
    }

    private int move(CommandContext<CommandSource> c, Vector3i pos, World world)
            throws CommandSyntaxException
    {
        Waypoint waypoint = get(c);

        if (waypoint.getPosition().equals(pos)
                && waypoint.getWorld().equals(world)
        ) {
            throw Exceptions.format("Waypoint is already located at world={0}, pos={1}",
                    world.getName(), pos
            );
        }

        waypoint.setPosition(pos, world);

        c.getSource().sendAdmin(
                Text.format("Moved waypoint to world={0}, pos={1}",
                        world.getName(), pos
                )
        );
        return 0;
    }
}