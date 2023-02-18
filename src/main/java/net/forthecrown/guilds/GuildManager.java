package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildSettings.UNLIMITED_CHUNKS;
import static net.forthecrown.guilds.Guilds.NO_EXP;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.core.Announcer;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.Messages;
import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.guilds.multiplier.ExpModifiers;
import net.forthecrown.guilds.unlockables.UnlockableColor;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.UUID2IntMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.waypoint.Waypoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.level.ChunkPos;

public class GuildManager {

  private static final GuildManager inst = new GuildManager();

  @Getter
  private final GuildDataStorage storage;

  /**
   * Map of packed chunk positions to the guilds that own them
   */
  private final Long2ObjectMap<Guild> byChunk = new Long2ObjectOpenHashMap<>();

  // Set of guilds
  private final Object2ObjectMap<UUID, Guild> byId = new Object2ObjectOpenHashMap<>();

  /**
   * Guild name to guild lookup map
   */
  final Object2ObjectMap<String, Guild> byName = new Object2ObjectOpenHashMap<>();

  /**
   * Transient guild id 2 total exp map, used for displaying in /guildtop
   */
  @Getter
  private transient final UUID2IntMap expTop = new UUID2IntMap();

  @Getter
  private final ExpModifiers expModifier = new ExpModifiers();

  @Getter
  private final BannerRenderer renderer;

  private GuildManager() {
    storage = new GuildDataStorage(PathUtil.getPluginDirectory("guilds"));

    expTop.setValidator(uuid -> {
      var guild = getGuild(uuid);

      return guild == null
          ? uuid + " doesn't belong to a guild!"
          : null;
    });

    ConfigManager.get().registerConfig(GuildConfig.class);

    try {
      this.renderer = new BannerRenderer();
    } catch (IOException exc) {
      throw new IllegalStateException(exc);
    }
  }

  public static GuildManager get() {
    return inst;
  }

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    resetDailyExpEarnedAmounts();

    var announcer = Announcer.get();

    // If start of weekend or end of weekend, announce multiplier state change
    if (time.getDayOfWeek() == DayOfWeek.SATURDAY) {
      Users.getOnline()
          .forEach(user -> {
            float mod = expModifier.getModifier(user.getUniqueId());
            user.sendMessage(Messages.weekendMultiplierActive(mod));
          });
    } else if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
      announcer.announce(Messages.WEEKEND_MULTIPLIER_INACTIVE);
    }
  }

  public void createGuild(User user, String guildName) {
    // Guild
    UUID guildId = UUID.randomUUID();

    GuildMember leader = new GuildMember(user.getUniqueId());
    leader.setRankId(ID_LEADER);
    leader.setJoinDate(System.currentTimeMillis());

    var guild = new Guild(guildId, NO_EXP, System.currentTimeMillis());
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

    user.setGuild(guild);
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
    byChunk.long2ObjectEntrySet()
        .removeIf(next -> {
          if (!next.getValue().equals(guild)) {
            return false;
          }

          ChunkPos pos = Guilds.chunkFromPacked(next.getLongKey());
          GuildDynmap.unrenderChunk(pos);
          return true;
        });

    storage.delete(guild.getId());
    Guilds.yeetWaypoint(guild);

    var discord = guild.getDiscord();
    discord.deleteRole();
    discord.archiveChannel("Guild-Deleted");
  }

  public void addGuild(Guild guild, LongSet packedChunks) {
    byId.put(guild.getId(), guild);

    if (!Strings.isNullOrEmpty(guild.getName())) {
      byName.put(guild.getName().toLowerCase(), guild);
    }

    if (guild.getTotalExp() > 0) {
      expTop.set(guild.getId(), (int) guild.getTotalExp());
    }

    packedChunks.forEach(value -> byChunk.put(value, guild));
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
    byChunk.clear();
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

  public Guild getOwner(ChunkPos pos) {
    return byChunk.get(pos.toLong());
  }

  // Get the amount of chunks the given guild has claimed
  public int getGuildChunkAmount(Guild guild) {
    return Math.toIntExact(
        byChunk.values().stream()
            .filter(id -> id.equals(guild))
            .count()
    );
  }

  public boolean canClaimMore(Guild guild) {
    if (guild.getSettings().hasFlags(UNLIMITED_CHUNKS)) {
      return true;
    }

    return getGuildChunkAmount(guild)
        < Upgradable.MAX_CHUNKS.currentLimit(guild);
  }

  // Get the chunks the given guild has claimed
  public LongSet getGuildChunks(Guild guild) {
    return LongOpenHashSet.toSet(
        byChunk
            .long2ObjectEntrySet()
            .stream()
            .filter(entry -> Objects.equals(guild, entry.getValue()))
            .mapToLong(Long2ObjectMap.Entry::getLongKey)
    );
  }

  public void setChunkOwner(Guild guild, ChunkPos pos) {
    byChunk.put(pos.toLong(), guild);

    if (DynmapUtil.isInstalled()) {
      if (guild == null) {
        GuildDynmap.unrenderChunk(pos);
      } else {
        GuildDynmap.renderChunk(pos, guild);
      }
    }
  }

  public void removeChunkOwner(Guild id, ChunkPos pos) {
    removeChunkOwner(id, pos.toLong());
  }

  public void removeChunkOwner(Guild guild, long pos) {
    if (!Objects.equals(byChunk.get(pos), guild)) {
      return;
    }

    byChunk.remove(pos);

    if (DynmapUtil.isInstalled()) {
      GuildDynmap.unrenderChunk(Guilds.chunkFromPacked(pos));
    }
  }

  // Data managing

  public void resetDailyExpEarnedAmounts() {
    byId.values().forEach(g -> g.getMembers().values().forEach(GuildMember::resetExpEarnedToday));
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  @OnSave
  public void save() {
    for (Guild g : byId.values()) {
      storage.saveGuild(g);
      storage.saveChunks(g.getId(), getGuildChunks(g));
    }

    storage.saveModifiers(getExpModifier());
  }

  @OnLoad
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

  public Component getGuildChunkMsg(ChunkPos centerChunk) {
    int centerX = centerChunk.x;
    int centerZ = centerChunk.z;

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
        Guild guild = byChunk.get(ChunkPos.asLong(i, j));

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
    text.append(indentNS)
        .append(text("S", NamedTextColor.GOLD));

    return text.build();
  }
}