package net.forthecrown.guilds;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.JsonUtils;
import net.kyori.adventure.text.Component;

@AllArgsConstructor
public class GuildRank {

  public static final int ID_MEMBER = 0;
  public static final int ID_LEADER = 6;

  public static final int RANK_COUNT = ID_LEADER + 1;

  public static final int NOT_SET = -1;

  private static final String FORMATTED_NAME_KEY = "formattedName";
  private static final String DESCRIPTION_KEY = "description";
  private static final String PERMS_KEY = "permissions";
  private static final String MAX_CHUNKS_KEY = "maxChunkClaims";
  private static final String AUTO_LEVELUP_KEY = "autoLevelUp";
  private static final String ALL_PERMS = "all";

  @Getter
  private final int id;

  @Getter @Setter
  private String name;

  private final String description;

  // Jules: Use EnumSet
  private final EnumSet<GuildPermission> permissions = EnumSet.noneOf(GuildPermission.class);

  @Getter @Setter
  private int maxChunkClaims = NOT_SET;

  @Getter @Setter
  private int totalExpLevelUp = NOT_SET;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  public GuildRank(int id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /* --------------------------- STATIC METHODS --------------------------- */

  public static GuildRank createLeader() {
    var rank = new GuildRank(ID_LEADER, "&fLeader", "Guild leader");
    rank.permissions.addAll(List.of(GuildPermission.values()));
    return rank;
  }

  public static GuildRank createDefault() {
    return new GuildRank(ID_MEMBER, "&8Member", "Default Rank");
  }

  /* ----------------------------- METHODS ------------------------------ */

  public String getDescription() {
    return this.description == null ? "Guild Rank" : this.description;
  }

  public boolean hasFormattedName() {
    return !Strings.isNullOrEmpty(name);
  }

  public Component getFormattedName() {
    return Text.renderString(name);
  }

  public boolean hasPermission(GuildPermission perm) {
    if (perm == null) {
      return true;
    }

    return this.permissions.contains(perm);
  }

  public boolean togglePermission(GuildPermission perm) {
    if (hasPermission(perm)) {
      this.permissions.remove(perm);
      return false;
    }

    this.permissions.add(perm);
    return true;
  }

  // Get GuildRank from Json
  public static GuildRank deserialize(JsonObject json, int id) {
    // Formatted name
    String formattedName = json.has(FORMATTED_NAME_KEY)
        ? json.get(FORMATTED_NAME_KEY).getAsString()
        : null;

    String description = json.has(DESCRIPTION_KEY)
        ? json.get(DESCRIPTION_KEY).getAsString()
        : null;

    var rank = new GuildRank(id, formattedName, description);

    int autoLevelUp = json.has(AUTO_LEVELUP_KEY)
        ? json.get(AUTO_LEVELUP_KEY).getAsInt()
        : NOT_SET;

    int maxChunkClaims = json.has(MAX_CHUNKS_KEY)
        ? json.get(MAX_CHUNKS_KEY).getAsInt()
        : NOT_SET;

    rank.setMaxChunkClaims(maxChunkClaims);
    rank.setTotalExpLevelUp(autoLevelUp);

    var perms = json.get(PERMS_KEY);

    if (perms != null) {
      // If 'all' string: Give all permissions
      if (perms.isJsonPrimitive()
          && perms.getAsJsonPrimitive().isString()
          && perms.getAsString().equalsIgnoreCase(ALL_PERMS)
      ) {
        rank.permissions.addAll(Arrays.asList(GuildPermission.values()));
      }
      // If array: read permissions
      else if (perms.isJsonArray()) {
        JsonUtils.stream(perms.getAsJsonArray())
            .map(element -> {
              try {
                return JsonUtils.readEnum(GuildPermission.class, element);
              } catch (IllegalArgumentException exc) {
                Loggers.getLogger().warn("Unknown permission: {}", element);
                return null;
              }
            })
            .filter(Objects::nonNull)
            .forEach(rank.permissions::add);
      }
      // Invalid element, warn the console
      else {
        Loggers.getLogger().error(
            "Invalid JSON element found for guild rank permissions: {}",
            perms
        );
      }
    }

    return rank;
  }

  // Get Json from GuildRank
  public JsonObject serialize() {
    JsonObject result = new JsonObject();

    // Formatted name
    if (hasFormattedName()) {
      result.addProperty(FORMATTED_NAME_KEY, this.name);
    }

    if (description != null) {
      result.addProperty(DESCRIPTION_KEY, this.description);
    }

    if (getMaxChunkClaims() != NOT_SET) {
      result.addProperty(MAX_CHUNKS_KEY, getMaxChunkClaims());
    }

    if (getTotalExpLevelUp() != NOT_SET) {
      result.addProperty(AUTO_LEVELUP_KEY, getTotalExpLevelUp());
    }

    // Permissions
    if (id == ID_LEADER
        || permissions.size() >= GuildPermission.values().length
    ) {
      result.addProperty(PERMS_KEY, ALL_PERMS);
    } else if (!permissions.isEmpty()) {
      result.add(PERMS_KEY,
          JsonUtils.ofStream(permissions.stream().map(JsonUtils::writeEnum))
      );
    }

    return result;
  }

  @Override
  public String toString() {
    return "GuildRank{" +
        ", formattedName='" + name + '\'' +
        ", permissions=" + permissions +
        '}';
  }
}