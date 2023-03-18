package net.forthecrown.commands.guild;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildSettings.GUILD_CHANNEL;
import static net.forthecrown.guilds.GuildSettings.ROLE_COLOR;
import static net.forthecrown.guilds.GuildSettings.UNLIMITED_CHUNKS;
import static net.forthecrown.guilds.GuildSettings.UNLIMITED_MEMBERS;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.DiscordUnlocks;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.Waypoints;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.kyori.adventure.text.format.NamedTextColor;

class GuildSetNode extends GuildCommandNode {

  public GuildSetNode() {
    super("guildset", "set");
    setAliases("gset");
  }

  private static final Predicate<CommandSource> CHANNEL_UNLOCKED = source -> {
    if (!source.isPlayer()) {
      return true;
    }

    var player = source.asPlayerOrNull();
    var user = Users.getLoadedUser(player.getUniqueId());

    if (user == null) {
      return false;
    }

    var guild = user.getGuild();

    if (guild == null) {
      return false;
    }

    return DiscordUnlocks.CHANNEL.isUnlocked(guild);
  };

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("name <name>", "Sets your guild's name");
    factory.usage("leader <player>", "Sets your guild's leader");

    factory.usage("waypoint")
        .addInfo("Sets your guild's waypoint to the waypoint you're looking")
        .addInfo("at, or the closest one to you");

    factory.usage("discordAnnouncements")
        .setCondition(CHANNEL_UNLOCKED)
        .addInfo("Sets whether guild announcements are")
        .addInfo("forwarded to the guild's discord channel");

    factory.usage("unlimitedMembers <guild> <true | false>")
        .setPermission(Permissions.GUILD_ADMIN)
        .addInfo("Sets whether a guild can have unlimited members");

    factory.usage("unlimitedMembers <guild>")
        .setPermission(Permissions.GUILD_ADMIN)
        .addInfo("Checks whether a guild has unlimited members.");

    factory.usage("unlimitedChunks <guild> <true | false>")
        .setPermission(Permissions.GUILD_ADMIN)
        .addInfo("Sets whether a guild can have unlimited chunks");

    factory.usage("unlimitedChunks <guild>")
        .setPermission(Permissions.GUILD_ADMIN)
        .addInfo("Checks whether a guild has unlimited chunks.");
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
        .then(flagArgument("unlimitedMembers", UNLIMITED_MEMBERS))
        .then(flagArgument("unlimitedChunks", UNLIMITED_CHUNKS))
        .then(flagArgument("roleColorFlag", ROLE_COLOR))
        .then(flagArgument("guildChannelFlag", GUILD_CHANNEL))

        .then(donatorArg("roleColor", ROLE_COLOR))
        .then(donatorArg("guildChannel", GUILD_CHANNEL))

        .then(createGuildCommand("discordAnnouncements", this::discordAnnouncements)
            .requires(CHANNEL_UNLOCKED)
        )

        .then(literal("name")
            .then(nameArg)
        )

        .then(literal("leader")
            .then(leaderArg)
        )

        .then(waypointArg);
  }

  private LiteralArgumentBuilder<CommandSource> donatorArg(String name, int flag) {
    return literal(name)
        .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

        .then(argument("player", Arguments.USER)
            .executes(c -> {
              var player = Arguments.getUser(c, "player");
              var guild = player.getGuild();

              if (guild == null) {
                throw Exceptions.NOT_IN_GUILD;
              }

              guild.getSettings().addFlags(flag);
              return 0;
            })
        );
  }

  private LiteralArgumentBuilder<CommandSource> flagArgument(String name,
                                                             int flag
  ) {
    return literal(name)
        .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))

        .then(guildArgument()
            .executes(c -> {
              Guild guild = providerForArgument().get(c);
              boolean set = guild.getSettings().hasFlags(flag);

              c.getSource().sendMessage(
                  Text.format("{0} set for {1}: {2}",
                      name,
                      guild.displayName(),
                      set
                  )
              );
              return 0;
            })

            .then(argument("state", BoolArgumentType.bool())
                .executes(c -> {
                  boolean state = c.getArgument("state", Boolean.class);
                  Guild guild = providerForArgument().get(c);

                  if (state) {
                    guild.getSettings().addFlags(flag);
                  } else {
                    guild.getSettings().removeFlags(flag);
                  }

                  c.getSource().sendSuccess(
                      Text.format("{0} now set for {1}: {2}",
                          name,
                          guild.displayName(),
                          state
                      )
                  );
                  return 0;
                })
            )
        );
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

    guild.announce(
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

    guild.announce(
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
      var top = Waypoints.findTopBlock(user.getPlayer());

      if (top != null) {
        Waypoints.tryCreate(c.getSource(), provider.simplify(c));
        return 0;
      }

      if (nearest == null) {
        throw Exceptions.FAR_FROM_WAYPOINT;
      } else {
        throw Exceptions.farFromWaypoint(nearest);
      }
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
    guild.moveWaypoint(nearest, user);
    return 0;
  }

  private int discordAnnouncements(CommandContext<CommandSource> c,
                                   GuildProvider provider
  ) throws CommandSyntaxException {
    var guild = provider.get(c);
    var user = getUserSender(c);

    testPermission(user, guild,
        GuildPermission.DISCORD,
        Exceptions.NO_PERMISSION
    );

    boolean state = !guild.getDiscord().forwardAnnouncements();

    guild.announce(
        Text.format(
            "&e{0, user}&r {1} message forwarding to discord.",
            state ? NamedTextColor.GOLD : NamedTextColor.GRAY,
            user, state ? "enabled" : "disabled"
        )
    );

    guild.getDiscord().forwardAnnouncements(state);
    return 0;
  }
}