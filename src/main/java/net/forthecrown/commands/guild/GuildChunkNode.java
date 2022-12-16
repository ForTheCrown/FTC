package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.utils.Particles;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.format.ComponentFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.waypoint.Waypoint;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.ChunkPos;
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
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("chunk list",    "Displays the chunks your guild owns");
        writer.field("chunk claim",   "Claims the chunk you're in for your guild");
        writer.field("chunk unclaim", "Unclaims the chunk you're in");
        writer.field("chunk unclaimall", "Unclaims ALL chunks owned by your guild");

        if (source.hasPermission(Permissions.GUILD_ADMIN)) {
            writer.field(
                    "chunk list <guild>",
                    "Lists the chunks of a guild"
            );
            writer.field(
                    "chunk claim <guild>",
                    "Claims the chunk you're in for a guild"
            );
            writer.field(
                    "chunk unclaim <guild>",
                    "Unclaims the chunk you're in for a guild"
            );
            writer.field(
                    "chunk unclaimall <guild>",
                    "Unclaims ALL the chunks of a guild"
            );
        }
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
            user.sendMessage(GuildManager.get().getGuildChunkMsg(cPos));
        });

        return 0;
    }

    private int listChunks(CommandContext<CommandSource> c, GuildProvider provider) throws CommandSyntaxException {
        var guild = provider.get(c);

        var chunks = GuildManager.get()
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
                                Util.conditionalPlural(chunks.size())
                        )
                )
                .add(
                        chunks.longStream()
                                .mapToObj(value -> {
                                    var cPos = Guilds.chunkFromPacked(value);
                                    int bX = Vectors.toBlock(cPos.x) + (Vectors.CHUNK_SIZE / 2);
                                    int bZ = Vectors.toBlock(cPos.z) + (Vectors.CHUNK_SIZE / 2);

                                    return ComponentFormat.FormatType.VECTOR
                                            .resolveArgument(Vector2i.from(bX, bZ), "");
                                })
                )
                .asComponent();

        c.getSource().sendMessage(text);
        return 0;
    }

    private int claimChunk(CommandContext<CommandSource> c, GuildProvider provider) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, Exceptions.CANNOT_CLAIM_CHUNKS);
        Guilds.testWorld(user.getWorld());

        int maxChunks = Upgradable.MAX_CHUNKS.currentLimit(guild);
        var manager = GuildManager.get();

        if (manager.getGuildChunkAmount(guild) >= maxChunks) {
            throw Exceptions.cannotClaimMoreChunks(guild, maxChunks);
        }

        ChunkPos pos = Guilds.getChunk(user.getLocation());
        var owner = manager.getOwner(pos);

        if (owner != null) {
            throw Exceptions.chunkAlreadyClaimed(owner);
        }

        manager.setChunkOwner(guild, pos);
        user.sendMessage(Messages.guildChunkClaimed(guild));

        drawChunkBounds(user.getWorld(), pos, true, user.getLocation());

        return 0;
    }

    private int unclaimChunk(CommandContext<CommandSource> c, GuildProvider provider) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, Exceptions.CANNOT_CLAIM_CHUNKS);
        Guilds.testWorld(user.getWorld());

        var pos = Guilds.getChunk(user.getLocation());
        var manager = GuildManager.get();
        var owner = manager.getOwner(pos);

        if (owner == null || !owner.equals(guild)) {
            throw Exceptions.cannotUnclaimChunk(guild);
        }

        manager.removeChunkOwner(guild, pos);

        if (!guild.isMember(user.getUniqueId())) {
            user.sendMessage(Messages.CHUNK_UNCLAIMED);
        }

        guild.sendMessage(
                Messages.guildUnclaimAnnouncement(
                        Vector2i.from(pos.x << Vectors.CHUNK_BITS, pos.z << Vectors.CHUNK_BITS),
                        user
                )
        );

        drawChunkBounds(user.getWorld(), pos, false, user.getLocation());
        Waypoint waypoint = guild.getSettings().getWaypoint();

        if (waypoint != null) {
            ChunkPos wChunk = Vectors.getChunk(waypoint.getPosition());

            if (wChunk.equals(pos)) {
                Guilds.yeetWaypoint(guild);

                guild.sendMessage(
                        Messages.GUILD_WAYPOINT_LOST
                );
            }
        }

        return 0;
    }

    private int unclaimAll(CommandContext<CommandSource> c,
                           GuildProvider provider
    ) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        testPermission(user, guild, GuildPermission.CAN_CLAIM_CHUNKS, Exceptions.CANNOT_CLAIM_CHUNKS);

        GuildManager manager = GuildManager.get();
        Guilds.yeetWaypoint(guild);

        LongSet allChunks = manager.getGuildChunks(guild);
        allChunks.forEach(value -> manager.removeChunkOwner(guild, value));

        guild.sendMessage(Messages.guildUnclaimAllAnnouncement(user));
        user.sendMessage(Messages.guildUnclaimAll(guild));

        return 0;
    }

    private void drawChunkBounds(World world, ChunkPos pos, boolean claim, Location location) {
        Vector3i min = Vector3i.from(
                Vectors.toBlock(pos.x),
                Util.MIN_Y,
                Vectors.toBlock(pos.z)
        );
        Vector3i max = min.add(Vectors.CHUNK_SIZE, 0, Vectors.CHUNK_SIZE)
                .withY(Util.MAX_Y);

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