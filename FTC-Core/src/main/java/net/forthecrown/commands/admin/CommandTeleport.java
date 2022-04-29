package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.Vector3;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.math.MathUtil;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandTeleport extends FtcCommand {
    public CommandTeleport(){
        super("fteleport", Crown.inst());

        setPermission(Permissions.HELPER);
        setAliases("tp", "teleport", "eteleport", "etp");
        setHelpListName("teleport");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("entity", EntityArgument.multipleEntities())
                        .then(argument("entity_to", EntityArgument.entity())
                                .executes(c -> {
                                    Entity entity = EntityArgument.getPlayer(c, "entity_to");
                                    Collection<Entity> entities = EntityArgument.getEntities(c, "entity");

                                    Component display = FtcFormatter.displayName(entity);
                                    if(entity.getType() == EntityType.PLAYER) display = UserManager.getUser(entity.getUniqueId()).nickDisplayName();

                                    return teleport(entities, entity.getLocation(), display, c.getSource());
                                })
                        )

                        .then(argument("location_to", PositionArgument.position())
                                .executes(c -> entityTeleport(c, false, false))

                                .then(argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                                        .executes(c -> entityTeleport(c, true, false))

                                        .then(argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                                                .executes(c -> entityTeleport(c, true, true))
                                        )
                                )

                                .then(literal("facing")
                                        .then(argument("facing_pos", PositionArgument.position())
                                                .executes(c -> teleportFacing(c, PositionArgument.getLocation(c, "facing_pos")))
                                        )

                                        .then(literal("facingEntity")
                                                .then(argument("facing_entity", EntityArgument.entity())
                                                        .executes(c -> teleportFacing(c, EntityArgument.getEntity(c, "facing_entity").getLocation()))
                                                )
                                        )
                                )
                        )
                )

                .then(argument("entity", EntityArgument.entity())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            Entity entity = EntityArgument.getEntity(c, "entity");

                            Component display = FtcFormatter.displayName(entity);
                            if(entity instanceof Player) display = UserManager.getUser(entity.getUniqueId()).nickDisplayName();
                            if(user.isTeleporting()) throw FtcExceptionProvider.create("You are already teleporting");

                            user.createTeleport(entity::getLocation, false, true, UserTeleport.Type.TELEPORT)
                                    .setAsync(false)
                                    .start(false);

                            c.getSource().sendAdmin(
                                    Component.text("Teleported ")
                                            .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                            .append(Component.text(" to "))
                                            .append(display.color(NamedTextColor.YELLOW))
                            );
                            return 0;
                        })
                )

                .then(argument("location", PositionArgument.position())
                        .executes(c -> teleport(c, false, false))

                        .then(argument("yaw", FloatArgumentType.floatArg(-180f, 180f))
                                .executes(c -> teleport(c, true, false))

                                .then(argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                                        .executes(c -> teleport(c, true, true))
                                )
                        )
                );
    }

    private int teleportFacing(CommandContext<CommandSource> c, Location facing) throws CommandSyntaxException {
        Location location = PositionArgument.getLocation(c, "location_to");

        Vector3 dif = MathUtil.toWorldEdit(location.clone().subtract(facing));

        location.setYaw((float) dif.toYaw());
        location.setPitch((float) dif.toPitch());

        Collection<Entity> entities = EntityArgument.getEntities(c, "entity");

        return teleport(entities, location, FtcFormatter.clickableLocationMessage(location, false), c.getSource());
    }

    private int entityTeleport(CommandContext<CommandSource> c, boolean yaw, boolean pitch) throws CommandSyntaxException {
        Location location = PositionArgument.getLocation(c, "location_to");
        Collection<Entity> entities = EntityArgument.getEntities(c, "entity");

        if(yaw) location.setYaw(c.getArgument("yaw", Float.class));
        if(pitch) location.setPitch(c.getArgument("pitch", Float.class));

        return teleport(entities, location, FtcFormatter.clickableLocationMessage(location, false), c.getSource());
    }

    private int teleport(CommandContext<CommandSource> c, boolean yawGiven, boolean pitchGiven) throws CommandSyntaxException {
        CrownUser user = getUserSender(c);
        Location loc = PositionArgument.getLocation(c, "location");

        if(yawGiven) loc.setYaw(c.getArgument("yaw", Float.class));
        if(pitchGiven) loc.setPitch(c.getArgument("pitch", Float.class));

        if(user.isTeleporting()) throw FtcExceptionProvider.create("You are already teleporting");

        user.createTeleport(() -> loc, false, true, UserTeleport.Type.TELEPORT)
                .setAsync(false)
                .start(false);

        c.getSource().sendAdmin(
                Component.text("Teleported ")
                        .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                        .append(Component.text(" to "))
                        .append(FtcFormatter.clickableLocationMessage(loc, false).color(NamedTextColor.YELLOW))
        );
        return 0;
    }

    private static final Var<Boolean> TP_SET_RETURN = Var.def("cmd_tp_tpSetsReturnLocation", VarTypes.BOOL, false);

    private int teleport(Collection<Entity> entities, Location location, Component destDisplayName, CommandSource source) throws CommandSyntaxException {
        if(entities.isEmpty()) throw FtcExceptionProvider.create("Found no one to teleport");

        int amount = 0;
        for (Entity e: entities){
            if(e.getType() != EntityType.PLAYER) {
                e.teleport(location);
                amount++;
                continue;
            }

            CrownUser user = UserManager.getUser(e.getUniqueId());
            if(user.isTeleporting()) continue;

            user.createTeleport(() -> location, false, true, UserTeleport.Type.TELEPORT)
                    .setSetReturn(TP_SET_RETURN.get())
                    .start(false);
            amount++;
        }

        Component entMsg = entities.size() > 1 ? Component.text(amount + " entities").color(NamedTextColor.YELLOW) : entDisplay(entities).color(NamedTextColor.YELLOW);

        source.sendAdmin(
                Component.text("Teleported ")
                        .append(entMsg)
                        .append(Component.text(" to "))
                        .append(destDisplayName.color(NamedTextColor.YELLOW))
        );
        return 0;
    }

    public Component entOrUserDisplayName(Entity entity){
        if(entity.getType() == EntityType.PLAYER) return UserManager.getUser(entity.getUniqueId()).nickDisplayName();
        return FtcFormatter.displayName(entity);
    }

    public Component entDisplay(Collection<Entity> entities){
        for (Entity entity : entities) {
            return entOrUserDisplayName(entity);
        }
        return null;
    }
}