package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.RANK_COUNT;

import com.google.gson.JsonObject;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild.BoostTier;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Icon;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.guilds.unlockables.DiscordUnlocks;
import net.forthecrown.guilds.util.DynmapUtil;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.slf4j.Logger;

public class GuildSettings {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String NAME_KEY = "name";
  public static final String WAYPOINT_KEY = "waypoint";
  public static final String RANKS_KEY = "ranks";
  public static final String PRIMARY_COLOR_KEY = "primaryColor";
  public static final String SECONDARY_COLOR_KEY = "secondaryColor";
  public static final String NAME_FORMAT_KEY = "nameFormat";
  public static final String BANNER_KEY = "banner";
  public static final String IS_PUBLIC_KEY = "isPublic";
  public static final String ALLOWS_VISIT_KEY = "allowsVisit";
  public static final String FLAGS_KEY = "adminFlags";

  public static final int UNLIMITED_MEMBERS = 0x1;
  public static final int UNLIMITED_CHUNKS  = 0x2;

  /** Flag stating a guild has unlocked the role color feature */
  public static final int ROLE_COLOR = 0x4;

  /** Flag stating this guild has the guild channel as an unlocked feature */
  public static final int GUILD_CHANNEL = 0x8;

  @Getter
  private final Guild guild;

  @Getter
  @Setter(AccessLevel.PACKAGE)
  String name;

  final GuildRank[] ranks = new GuildRank[RANK_COUNT];

  @Getter
  private GuildColor primaryColor = GuildColor.WHITE;
  @Getter
  private GuildColor secondaryColor = GuildColor.LIGHT_GRAY;
  @Getter
  private final GuildNameFormat nameFormat = GuildNameFormat.createDefault();

  @Getter
  private ItemStack banner = new ItemStack(Material.WHITE_BANNER);

  @Setter
  private UUID waypoint;

  @Getter
  @Setter
  private boolean isPublic;

  @Getter
  @Setter
  @Accessors(fluent = true)
  private boolean allowsVisit;

  @Getter
  @Setter
  private int adminFlags;

  public GuildSettings(Guild guild) {
    this.guild = guild;
  }

  /* ------------------------------- FLAGS -------------------------------- */

  public boolean hasFlags(int flags) {
    return (adminFlags & flags) == flags;
  }

  public void addFlags(int flags) {
    adminFlags |= flags;
  }

  public void removeFlags(int flags) {
    adminFlags &= ~flags;
  }

  /* ------------------------------ METHODS ------------------------------- */

  public Waypoint getWaypoint() {
    if (waypoint == null) {
      return null;
    }

    return WaypointManager.getInstance().get(waypoint);
  }

  public GuildRank getRank(int id) {
    if (!hasRank(id)) {
      return null;
    }

    return ranks[id];
  }

  public void addRank(int id) {
    if (hasRank(id)) {
      return;
    }

    ranks[id] = new GuildRank(id, "NewRank", "Guild Rank");
  }

  public void addRank(GuildRank rank) {
    if (hasRank(rank.getId())) {
      return;
    }

    setRank(rank.getId(), rank);
  }

  public void setRank(int id, GuildRank rank) {
    Objects.checkIndex(id, ranks.length);
    ranks[id] = rank;
  }

  public boolean hasRank(int id) {
    return id >= 0
        && id < ranks.length
        && ranks[id] != null;
  }

  public void setPrimaryColor(GuildColor color) {
    if (color == this.primaryColor) {
      return;
    }

    this.primaryColor = color;

    // Update dynmap
    if (DynmapUtil.isInstalled()) {
      GuildDynmap.updateGuildChunks(getGuild());
    }

    // Only change color for donators
    if (!guild.getSettings().hasFlags(ROLE_COLOR)) {
      return;
    }

    guild.getDiscord().getRole().ifPresent(role -> {
      role.getManager()
          .setColor(color.getTextColor().value())
          .submit();
    });
  }

  public void setSecondaryColor(GuildColor color) {
    if (color == this.secondaryColor) {
      return;
    }

    this.secondaryColor = color;

    // Update dynmap
    if (DynmapUtil.isInstalled()) {
      GuildDynmap.updateGuildChunks(getGuild());
    }
  }

  public void setBanner(ItemStack item) {
    if (!(item.getItemMeta() instanceof BannerMeta meta)) {
      throw new IllegalArgumentException("Given item is not a banner");
    }

    banner.setType(item.getType());
    banner.editMeta(BannerMeta.class, banner -> {
      banner.setPatterns(meta.getPatterns());
    });

    var disc = guild.getDiscord();
    var boost = GuildDiscord.getDiscordGuild().getBoostTier();

    if (boost == BoostTier.NONE
        || boost == BoostTier.TIER_1
        || !DiscordUnlocks.COLOR.isUnlocked(guild) // Check if donator
    ) {
      return;
    }

    disc.getRole().ifPresent(role -> {
      try {
        Icon icon = disc.getIcon();
        role.getManager()
            .setIcon(icon)
            .submit()
            .whenComplete((unused, throwable) -> {
              if (throwable == null) {
                return;
              }

              LOGGER.error("Couldn't set guild icon for {}",
                  guild, throwable
              );
            });
      } catch (IOException exc) {
        LOGGER.error("Couldn't create role icon for {}", guild, exc);
      }
    });
  }

  // Get GuildSettings from Json
  public void deserialize(JsonObject jsonObject) {
    var json = JsonWrapper.wrap(jsonObject);

    name = json.getString(NAME_KEY);

    // Guild ranks
    JsonObject ranksJson = json.get(RANKS_KEY).getAsJsonObject();
    for (var e : ranksJson.entrySet()) {
      int id = Integer.parseInt(e.getKey());
      var rank = GuildRank.deserialize(e.getValue().getAsJsonObject(), id);

      setRank(id, rank);
    }

    setPublic(json.get(IS_PUBLIC_KEY).getAsBoolean());
    allowsVisit(json.get(ALLOWS_VISIT_KEY).getAsBoolean());

    primaryColor = json.getEnum(PRIMARY_COLOR_KEY, GuildColor.class);
    secondaryColor = json.getEnum(SECONDARY_COLOR_KEY, GuildColor.class);
    banner = json.getItem(BANNER_KEY);
    adminFlags = json.getInt(FLAGS_KEY);
    setWaypoint(json.getUUID(WAYPOINT_KEY));

    if (json.has(NAME_FORMAT_KEY)) {
      nameFormat.deserialize(json.get(NAME_FORMAT_KEY));
    }
  }

  // Get Json from GuildSettings
  public JsonObject serialize() {
    JsonObject result = new JsonObject();

    result.addProperty(NAME_KEY, this.name);

    if (waypoint != null) {
      result.add(WAYPOINT_KEY, JsonUtils.writeUUID(waypoint));
    }

    // Guild ranks
    JsonObject ranksJson = new JsonObject();

    var it = ArrayIterator.unmodifiable(ranks);
    while (it.hasNext()) {
      var id = it.nextIndex();
      ranksJson.add(String.valueOf(id), it.next().serialize());
    }
    result.add(RANKS_KEY, ranksJson);

    result.addProperty(PRIMARY_COLOR_KEY, this.primaryColor.name());
    result.addProperty(SECONDARY_COLOR_KEY, this.secondaryColor.name());

    result.add(BANNER_KEY, JsonUtils.writeItem(this.banner));
    result.addProperty(IS_PUBLIC_KEY, this.isPublic);
    result.addProperty(ALLOWS_VISIT_KEY, this.allowsVisit);

    if (adminFlags != 0) {
      result.addProperty(FLAGS_KEY, adminFlags);
    }

    if (!nameFormat.isDefault()) {
      result.add(NAME_FORMAT_KEY, nameFormat.serialize());
    }

    return result;
  }
}