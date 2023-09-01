package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildSettings.UNLIMITED_CHUNKS;
import static net.forthecrown.guilds.Guilds.NO_EXP;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.guilds.multiplier.ExpModifiers;
import net.forthecrown.guilds.unlockables.UnlockableColor;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.guilds.util.DynmapUtil;
import net.forthecrown.user.User;
import net.forthecrown.utils.ScoreIntMap;
import net.forthecrown.utils.collision.ChunkCollisionMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.math.vector.Vector2i;

public class  GuildManager {

  @Getter
  private final GuildPlugin plugin;

  @Getter
  private final GuildDataStorage storage;

  @Getter
  private final ChunkCollisionMap<Guild> chunkMap = new ChunkCollisionMap<>();

  private final Object2ObjectMap<UUID, Guild> byId = new Object2ObjectOpenHashMap<>();
  final Object2ObjectMap<String, Guild> byName = new Object2ObjectOpenHashMap<>();

  /**
   * Transient guild id 2 total exp map, used for displaying in /guildtop
   */
  @Getter
  private transient final ScoreIntMap<UUID> expTop = new ScoreIntMap<>();

  @Getter
  private final ExpModifiers expModifier;

  @Getter
  private final BannerRenderer renderer;

  GuildManager(GuildPlugin plugin) {
    this.plugin = plugin;
    this.storage = new GuildDataStorage(PathUtil.pluginPath());

    this.expModifier = new ExpModifiers(this);

    expTop.setValidator(uuid -> {
      var guild = getGuild(uuid);

      return guild == null
          ? uuid + " doesn't belong to a guild!"
          : null;
    });

    try {
      this.renderer = new BannerRenderer();
    } catch (IOException exc) {
      throw new IllegalStateException(exc);
    }
  }

  public void createGuild(User user, String guildName) {
    // Guild
    UUID guildId = UUID.randomUUID();

    var guild = new Guild(guildId, NO_EXP, System.currentTimeMillis());

    GuildMember leader = new GuildMember(user.getUniqueId(), guild);
    leader.setRankId(ID_LEADER);
    leader.setJoinDate(System.currentTimeMillis());

    guild.members.put(leader.getId(), leader);

    // Settings
    GuildSettings settings = guild.getSettings();
    settings.addRank(GuildRank.createLeader());
    settings.addRank(GuildRank.createDefault());
    settings.setName(guildName);

    var unlockables = guild.getUnlockables();

    // Add defaults as unlocked
    unlockables.setExpProgress(
        UnlockableColor.WHITE.getPrimaryOption(),
        UnlockableColor.WHITE.getExpRequired()
    );

    unlockables.setExpProgress(
        UnlockableColor.LIGHT_GRAY.getSecondaryOption(),
        UnlockableColor.LIGHT_GRAY.getExpRequired()
    );

    user.set(GUserProperties.GUILD, guildId);
    addGuild(guild, LongSets.emptySet());
  }

  public void removeGuild(UUID id) {
    var guild = byId.remove(id);

    if (guild == null) {
      return;
    }

    if (!Strings.isNullOrEmpty(guild.getName())) {
      byName.remove(guild.getName().toLowerCase());
    }

    expTop.remove(id);

    // Un-render all guild chunks and
    // remove guild from chunk lookup map
    chunkMap.entrySet()
        .removeIf(next -> {
          if (!next.getValue().equals(guild)) {
            return false;
          }

          var pos = Guilds.chunkFromPacked(next.getLongKey());
          GuildDynmap.unrenderChunk(pos);

          return true;
        });

    storage.delete(guild.getId());
    Guilds.yeetWaypoint(guild);

    var discord = guild.getDiscord();

    discord.getRole().ifPresent(role -> {
      discord.deleteRole();
    });


    discord.channelIfNotArchived().ifPresent(channel -> {
      discord.archiveChannel("Guild-Deleted");
    });
  }

  public void addGuild(Guild guild, LongSet packedChunks) {
    byId.put(guild.getId(), guild);

    if (!Strings.isNullOrEmpty(guild.getName())) {
      byName.put(guild.getName().toLowerCase(), guild);
    }

    if (guild.getTotalExp() > 0) {
      expTop.set(guild.getId(), (int) guild.getTotalExp());
    }

    packedChunks.forEach(value -> chunkMap.put(value, guild));
    expTop.set(guild.getId(), (int) guild.getTotalExp());
  }

  void onRename(String name, Guild guild) {
    String oldName = Strings.nullToEmpty(guild.getSettings().getName())
        .toLowerCase();

    if (!Strings.isNullOrEmpty(oldName)) {
      byName.remove(oldName);
    }

    if (!Strings.isNullOrEmpty(name)) {
      byName.put(name.toLowerCase(), guild);

    }

    var waypoint = guild.getSettings().getWaypoint();
    if (waypoint != null) {
      Waypoints.updateDynmap(waypoint);
    }
  }

  public void clear() {
    byId.clear();
    byName.clear();
    chunkMap.clear();
  }

  public Guild getGuild(UUID id) {
    return byId.get(id);
  }

  public Guild getGuild(String name) {
    return byName.get(name.toLowerCase());
  }

  public int amount() {
    return this.byId.size();
  }

  public ObjectSet<String> getGuildNames() {
    return ObjectSets.unmodifiable(byName.keySet());
  }

  public List<Guild> getGuilds() {
    return new ObjectArrayList<>(byId.values());
  }

  // Guild Chunks

  public Guild getOwner(Vector2i pos) {
    return chunkMap.get(pos.x(), pos.y());
  }

  // Get the amount of chunks the given guild has claimed
  public int getGuildChunkAmount(Guild guild) {
    return chunkMap.getChunks(guild).size();
  }

  public boolean canClaimMore(Guild guild) {
    if (guild.getSettings().hasFlags(UNLIMITED_CHUNKS)) {
      return true;
    }

    int chunks = getGuildChunkAmount(guild);
    return chunks < Upgradable.MAX_CHUNKS.currentLimit(guild);
  }

  // Get the chunks the given guild has claimed
  public LongSet getGuildChunks(Guild guild) {
    return chunkMap.getChunks(guild);
  }

  public void setChunkOwner(Guild guild, Vector2i pos) {
    chunkMap.put(guild, pos.x(), pos.y());

    if (DynmapUtil.isInstalled()) {
      if (guild == null) {
        GuildDynmap.unrenderChunk(pos);
      } else {
        GuildDynmap.renderChunk(pos, guild);
      }
    }
  }

  public void removeChunkOwner(Guild id, Vector2i pos) {
    removeChunkOwner(id, Vectors.toChunkLong(pos));
  }

  public void removeChunkOwner(Guild guild, long pos) {
    if (!Objects.equals(chunkMap.get(pos), guild)) {
      return;
    }

    chunkMap.remove(pos);

    if (DynmapUtil.isInstalled()) {
      GuildDynmap.unrenderChunk(Guilds.chunkFromPacked(pos));
    }
  }

  // Data managing

  public void resetDailyExpEarnedAmounts() {
    byId.values().forEach(g -> g.getMembers().values().forEach(GuildMember::resetExpEarnedToday));
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void save() {
    for (Guild g : byId.values()) {
      storage.saveGuild(g);
      storage.saveChunks(g.getId(), getGuildChunks(g));
    }

    storage.saveModifiers(getExpModifier());
  }

  public void load() {
    clear();

    storage.loadModifiers(expModifier);
    storage.findExistingGuilds().forEach(uuid -> {
      Guild guild = storage.loadGuild(uuid);

      LongSet chunks = storage.loadChunks(uuid)
          .resultOrPartial(Loggers.getLogger()::error)
          .orElseGet(LongSets::emptySet);

      addGuild(guild, chunks);
    });
  }

  public Component getGuildChunkMsg(Vector2i centerChunk) {
    int centerX = centerChunk.x();
    int centerZ = centerChunk.y();

    // Jules: Static-import text() method
    Component indentNS = text("           ")
        .append(text(" ").decorate(TextDecoration.BOLD));

    String indentW = "   ";

    TextComponent.Builder text = text()
        .append(indentNS)
        .append(text("N").color(NamedTextColor.GOLD))
        .append(Component.newline());

    for (int j = centerZ - 4; j <= centerZ + 4; j++) {
      TextComponent.Builder chunkRow = text();

      // Add West-indicator
      chunkRow.append(text(j == centerZ ? "W-" : indentW).color(NamedTextColor.GOLD));

      // Add row of chunks
      for (int i = centerX - 4; i <= centerX + 4; i++) {
        Guild guild = chunkMap.get(i, j);

        // You are here marker
        if (j == centerZ && i == centerX) {
          var middleText = text("☀")
              .color(NamedTextColor.GOLD)
              .hoverEvent(text("You are here"));

          // Jules: Color middle sun appropriately, if
          //   it's owned by a guild
          if (guild != null) {
            middleText = middleText
                .color(guild.getSettings().getPrimaryColor().getTextColor())
                .hoverEvent(
                    text()
                        .append(
                            text("You are here"),
                            Component.newline(),
                            guild.displayName()
                        )
                        .build()

                );
          }

          chunkRow.append(middleText);
          continue;
        }

        if (guild != null) {
          chunkRow.append(text("█")
              .color(guild.getSettings().getPrimaryColor().getTextColor())
              .hoverEvent(guild.displayName()));
        } else {
          chunkRow.append(text("▓")
              .color(NamedTextColor.GRAY)
              .hoverEvent(null));
        }
      }

      // Add East-indicator
      if (j == centerZ) {
        chunkRow.append(text("-E").color(NamedTextColor.GOLD));
      }

      // Add to overall msg
      chunkRow.append(Component.newline());
      text.append(chunkRow);
    }

    // Add South-indicator
    text.append(indentNS).append(text("S", NamedTextColor.GOLD));
    return text.build();
  }
}