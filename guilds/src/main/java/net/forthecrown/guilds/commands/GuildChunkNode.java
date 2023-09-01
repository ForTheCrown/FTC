package net.forthecrown.guilds.commands;

import static net.forthecrown.guilds.GuildRank.NOT_SET;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.forthecrown.McConstants;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMessages;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.format.TextFormatTypes;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoint;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

class GuildChunkNode extends GuildCommandNode {

  public GuildChunkNode() {
    super("guildchunks", "chunks");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("list", "Displays thechunks your guild owns");
    factory.usage("claim", "Claims the chunk you're in for your guild");
    factory.usage("unclaim", "Unclaims the chunk you're in for your guild");
    factory.usage("unclaimall", "Unclaims ALL chunks owned by your guild");

    factory.usage("list <guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("List the chunks of a guild");

    factory.usage("claim <guild")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Claims the chunk you're in for a guild");

    factory.usage("unclaim <guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Unclaims the chunk you're in for a guild");

    factory.usage("unclaimall <guild>")
        .setPermission(GuildPermissions.GUILD_ADMIN)
        .addInfo("Unclaims all the chunks of a guild");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .executes(this::chunkMap)

        .then(literal("map")
            .executes(this::chunkMap)
        )

        .then(createGuildCommand("list", this::listChunks))
        .then(createGuildCommand("claim", this::claimChunk))
        .then(createGuildCommand("unclaim", this::unclaimChunk))
        .then(createGuildCommand("unclaimall", this::unclaimAll));
  }

  private int chunkMap(CommandContext<CommandSource> c) throws CommandSyntaxException {
    var user = getUserSender(c);

    Tasks.runAsync(() -> {
      var cPos = Guilds.getChunk(user.getLocation());
      user.sendMessage(Guilds.getManager().getGuildChunkMsg(cPos));
    });

    return 0;
  }

  private int listChunks(CommandContext<CommandSource> c, GuildProvider provider)
      throws CommandSyntaxException {
    var guild = provider.get(c);

    var chunks = Guilds.getManager()
        .getGuildChunks(guild);

    if (chunks.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    var text = TextJoiner.onNewLine()
        .setPrefix(
            Text.format("{0} guild owns {1, number} chunk{2}. Displaying the center coordinates:\n",
                NamedTextColor.GRAY,

                guild.displayName(),
                chunks.size(),
                Text.conditionalPlural(chunks.size())
            )
        )
        .add(
            chunks.longStream()
                .mapToObj(value -> {
                  Vector2i cPos = Guilds.chunkFromPacked(value);
                  Vector2i pos = cPos.mul(Vectors.CHUNK_SIZE);

                  return TextFormatTypes.VECTOR.resolve(pos, "", null);
                })
        )
        .asComponent();

    c.getSource().sendMessage(text);
    return 0;
  }

  private int claimChunk(CommandContext<CommandSource> c, GuildProvider provider)
      throws CommandSyntaxException
  {
    var guild = provider.get(c);
    var user = getUserSender(c);

    testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, GuildExceptions.CANNOT_CLAIM_CHUNKS);
    Guilds.testWorld(user.getWorld());

    int maxChunks = Upgradable.MAX_CHUNKS.currentLimit(guild);
    var manager = Guilds.getManager();

    if (!manager.canClaimMore(guild)) {
      throw GuildExceptions.cannotClaimMoreChunks(guild, maxChunks);
    }

    Vector2i pos = Guilds.getChunk(user.getLocation());
    var owner = manager.getOwner(pos);

    if (owner != null) {
      throw GuildExceptions.chunkAlreadyClaimed(owner);
    }

    var member = guild.getMember(user.getUniqueId());

    if (member != null) {
      var rankId = member.getRankId();
      var rank = guild.getSettings().getRank(rankId);
      int maxChunkClaims = rank.getMaxChunkClaims();

      if (maxChunkClaims != NOT_SET
          && maxChunkClaims >= member.getClaimedChunks()
      ) {
        throw Exceptions.format(
            "Cannot claim more chunks! (Over rank limit of {0, number})",
            maxChunkClaims
        );
      }

      member.setClaimedChunks(member.getClaimedChunks() + 1);
    }

    manager.setChunkOwner(guild, pos);
    user.sendMessage(GuildMessages.guildChunkClaimed(guild));

    drawChunkBounds(user.getWorld(), pos, true, user.getLocation());

    return 0;
  }

  private int unclaimChunk(CommandContext<CommandSource> c, GuildProvider provider)
      throws CommandSyntaxException
  {
    var guild = provider.get(c);
    var user = getUserSender(c);

    testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, GuildExceptions.CANNOT_CLAIM_CHUNKS);
    Guilds.testWorld(user.getWorld());

    var pos = Guilds.getChunk(user.getLocation());
    var manager = Guilds.getManager();
    var owner = manager.getOwner(pos);

    if (owner == null || !owner.equals(guild)) {
      throw GuildExceptions.cannotUnclaimChunk(guild);
    }

    manager.removeChunkOwner(guild, pos);

    if (!guild.isMember(user.getUniqueId())) {
      user.sendMessage(GuildMessages.CHUNK_UNCLAIMED);

      var member = guild.getMember(user.getUniqueId());
      member.setClaimedChunks(member.getClaimedChunks() - 1);
    }

    guild.announce(
        GuildMessages.guildUnclaimAnnouncement(
            Vector2i.from(pos.x() << Vectors.CHUNK_BITS, pos.y() << Vectors.CHUNK_BITS),
            user
        )
    );

    drawChunkBounds(user.getWorld(), pos, false, user.getLocation());
    Waypoint waypoint = guild.getSettings().getWaypoint();

    if (waypoint != null) {
      Vector2i wChunk = Vectors.getChunk(waypoint.getPosition());

      if (wChunk.equals(pos)) {
        Guilds.yeetWaypoint(guild);
        guild.announce(GuildMessages.GUILD_WAYPOINT_LOST);
      }
    }

    return 0;
  }

  private int unclaimAll(CommandContext<CommandSource> c,
                         GuildProvider provider
  ) throws CommandSyntaxException {
    var guild = provider.get(c);
    var user = getUserSender(c);

    testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, GuildExceptions.CANNOT_CLAIM_CHUNKS);

    GuildManager manager = Guilds.getManager();
    Guilds.yeetWaypoint(guild);

    LongSet allChunks = manager.getGuildChunks(guild);
    allChunks.forEach(value -> manager.removeChunkOwner(guild, value));

    guild.announce(GuildMessages.guildUnclaimAllAnnouncement(user));
    user.sendMessage(GuildMessages.guildUnclaimAll(guild));

    return 0;
  }

  private void drawChunkBounds(World world, Vector2i pos, boolean claim, Location location) {
    Vector3i min = Vector3i.from(
        Vectors.toBlock(pos.x()),
        McConstants.MIN_Y,
        Vectors.toBlock(pos.y())
    );

    Vector3i max = min.add(Vectors.CHUNK_SIZE, 0, Vectors.CHUNK_SIZE)
        .withY(McConstants.MAX_Y);

    int yStart = location.getBlockY() - 5;
    int yEnd = location.getBlockY() + 20;

    for (int y = yStart; y < yEnd; y++) {
      var bounds = Bounds3i.of(min.withY(y), max.withY(y));

      Particles.drawBounds(
          world,
          bounds,
          (claim ? Particle.END_ROD : Particle.FALLING_LAVA)
              .builder()
              .extra(0F)
      );
    }
  }
}