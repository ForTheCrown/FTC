package net.forthecrown.commands.guild;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.Waypoints;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Objects;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;

class GuildSetNode extends GuildCommandNode {
    public GuildSetNode() {
        super("guildset", "set");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("set name <name>", "Sets your guild name");
        writer.field("set leader <user>", "Sets your guild's leader");

        writer.field("set waypoint", "Sets your guilds waypoint to the one " +
                "you're looking at, or the one you're close to"
        );

        if (source.hasPermission(Permissions.GUILD_ADMIN)) {
            writer.field("set name <name> <guild>", "Sets a guild's name");
            writer.field("set leader <user> <guild>", "Sets a guild's leader");
            writer.field("set waypoint <guild>", "Sets the guild's waypoint");
        }
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        var nameArg = argument("name", StringArgumentType.word());
        addGuildCommand(nameArg, this::rename);

        var leaderArg = argument("user", Arguments.USER);
        addGuildCommand(leaderArg, this::leader);

        var waypointArg = literal("waypoint");
        addGuildCommand(waypointArg, this::waypoint);

        command
                .then(literal("name")
                        .then(nameArg)
                )

                .then(literal("leader")
                        .then(leaderArg)
                )

                .then(waypointArg);
    }

    private int rename(CommandContext<CommandSource> c,
                       GuildProvider provider
    ) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        var name = c.getArgument("name", String.class);

        if (Objects.equals(guild.getName(), name)) {
            throw Exceptions.NOTHING_CHANGED;
        }

        testPermission(user, guild, GuildPermission.CAN_RENAME, Exceptions.NO_PERMISSION);
        Guilds.validateName(name);

        guild.rename(name);

        guild.sendMessage(
                Text.format("&e{0, user}&r renamed the guild to '&6{1}&r'",
                        NamedTextColor.GRAY,
                        user, name
                )
        );

        if (guild.isMember(user.getUniqueId())) {
            user.sendMessage(
                    Text.format("Renamed guild &f{0}&r.",
                            NamedTextColor.GRAY,
                            guild.displayName()
                    )
            );
        }

        return 0;
    }

    private int leader(CommandContext<CommandSource> c,
                       GuildProvider provider
    ) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        var target = Arguments.getUser(c, "user");
        var targetMember = guild.getMember(target.getUniqueId());

        if (targetMember == null) {
            throw Exceptions.notGuildMember(target, guild);
        }

        var userMember = guild.getMember(user.getUniqueId());

        if (userMember == null) {
            if (!user.hasPermission(Permissions.GUILD_ADMIN)) {
                throw Exceptions.NO_PERMISSION;
            }

            var owner = guild.getLeader();
            owner.setRankId(targetMember.getRankId());

            user.sendMessage(
                    Text.format("Gave {0} guild's leadership to {1, user}",
                            guild, target
                    )
            );
        } else {
            if (userMember.getRankId() != ID_LEADER
                    && !user.hasPermission(Permissions.GUILD_ADMIN)
            ) {
                throw Exceptions.NO_PERMISSION;
            }

            if (target.equals(user)) {
                throw Exceptions.PROMOTE_SELF;
            }

            userMember.setRankId(targetMember.getRankId());
        }

        targetMember.setRankId(ID_LEADER);

        guild.sendMessage(
                Text.format("&e{0, user}&r has given guild leadership to &6{1, user}&r.",
                        NamedTextColor.GRAY,
                        user, target
                )
        );
        return 0;
    }

    private int waypoint(CommandContext<CommandSource> c,
                         GuildProvider provider
    ) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        testPermission(
                user,
                guild,
                GuildPermission.CAN_RELOCATE,
                Exceptions.G_NO_PERM_WAYPOINT
        );

        Waypoint nearest = Waypoints.getNearest(user);

        if (nearest == null
                || !nearest.getBounds().contains(user.getPlayer())
        ) {
            Waypoints.tryCreate(c.getSource(), provider.simplify(c));
            return 0;
        }

        if (nearest.getType() != WaypointTypes.PLAYER
                && nearest.getType() != WaypointTypes.GUILD
        ) {
            throw Exceptions.format(
                    "Cannot set {0} waypoint as guild home!",
                    nearest.getType().getDisplayName()
            );
        }

        var opt = Waypoints.isValidWaypointArea(
                nearest.getPosition(),
                WaypointTypes.GUILD,
                nearest.getWorld(),
                false
        );

        if (opt.isPresent()) {
            throw opt.get();
        }

        var manager = GuildManager.get();
        var cPos = Vectors.getChunk(nearest.getPosition());

        var owner = manager.getOwner(cPos);

        if (!Objects.equals(guild, owner)) {
            throw Exceptions.G_EXTERNAL_WAYPOINT;
        }

        nearest.setType(WaypointTypes.GUILD);
        Waypoints.setGuildWaypoint(guild, nearest, user);
        return 0;
    }
}