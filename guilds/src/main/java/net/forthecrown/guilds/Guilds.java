package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildNameFormat.GUILD_NAME;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Objects;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import net.forthecrown.Worlds;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.command.Exceptions;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.spongepowered.math.vector.Vector2i;

public @UtilityClass class Guilds {
  /* ---------------------------- SERIAL KEYS ----------------------------- */

  public final String
      KEY_SLOT = "slot",
      KEY_ITEM = "item";

  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final int NO_EXP = 0;

  /* ------------------------------ UTILITY ------------------------------- */

  public static GuildPlugin getPlugin() {
    return GuildPlugin.get();
  }

  public static GuildConfig getConfig() {
    return getPlugin().getGuildConfig();
  }

  public static GuildManager getManager() {
    return getPlugin().getManager();
  }

  public World getWorld() {
    return Worlds.overworld();
  }

  public static Guild getGuild(Player player) {
    return getGuild(Users.get(player));
  }

  public static Guild getStandingIn(Player player) {
    var map = getManager().getChunkMap();
    return map.get(player.getLocation());
  }

  public static Guild getStandingInOwn(Player player) {
    var standingIn = getStandingIn(player);
    var own = getGuild(player);

    if (Objects.equals(own, standingIn)) {
      return standingIn;
    }

    return null;
  }

  public static Guild getGuild(UUID playerId) {
    return getGuild(Users.get(playerId));
  }

  public static Guild getGuild(User user) {
    if (!user.has(GUserProperties.GUILD)) {
      return null;
    }

    UUID guildId = user.get(GUserProperties.GUILD);
    return getManager().getGuild(guildId);
  }

  public void removeAndArchive(Guild guild, String source, String reason) {
    // Clear members
    guild.getMembers()
        .keySet()
        .stream()
        .map(Users::get)

        .forEach(u -> {
          var userGuild = getGuild(u);

          if (userGuild == null || !userGuild.equals(guild)) {
            return;
          }

          u.set(GUserProperties.GUILD, null);
        });

    GuildManager manager = getManager();
    LongSet chunks = manager.getGuildChunks(guild);

    manager.removeGuild(guild.getId());
    manager.getStorage()
        .archive(
            guild,
            System.currentTimeMillis(),
            source,
            reason,
            chunks
        );
  }

  public void yeetWaypoint(Guild guild) {
    Waypoint waypoint = guild.getSettings().getWaypoint();
    guild.getSettings().setWaypoint(null);

    if (waypoint == null) {
      return;
    }

    WaypointManager.getInstance()
        .removeWaypoint(waypoint);
  }

  /* --------------------------- CHUNK UTILITY ---------------------------- */

  public Vector2i getChunk(Location location) {
    return new Vector2i(
        Vectors.toChunk(location.getBlockX()),
        Vectors.toChunk(location.getBlockZ())
    );
  }

  public Vector2i chunkFromPacked(long l) {
    return Vectors.fromChunkLong(l);
  }

  /* ---------------------- INVENTORY SERIALIZATION ----------------------- */

  public JsonArray writeInventory(Inventory inventory) {
    var it = ItemStacks.nonEmptyIterator(inventory);
    JsonArray arr = new JsonArray();

    while (it.hasNext()) {
      var index = it.nextIndex();
      var item = it.next();

      var obj = new JsonObject();
      obj.addProperty(KEY_SLOT, index);
      obj.add(KEY_ITEM, JsonUtils.writeItem(item));

      arr.add(obj);
    }

    return arr;
  }

  public void readInventory(Inventory into, JsonElement element) {
    into.clear();

    if (element == null) {
      return;
    }

    var arr = element.getAsJsonArray();

    if (arr.isEmpty()) {
      return;
    }

    for (var e : arr) {
      var obj = e.getAsJsonObject();

      int slot = obj.get(KEY_SLOT).getAsInt();
      var item = JsonUtils.readItem(obj.get(KEY_ITEM));

      into.setItem(slot, item);
    }
  }

  public static void testWorld(World world) throws CommandSyntaxException {
    if (world.equals(getWorld())) {
      return;
    }

    throw GuildExceptions.GUILDS_WRONG_WORLD;
  }

  public static void validateName(String name) throws CommandSyntaxException {
    if (name.length() < Guild.MIN_NAME_SIZE) {
      throw GuildExceptions.guildNameSmall(name);
    }

    if (name.length() > Guild.MAX_NAME_SIZE) {
      throw GuildExceptions.guildNameLarge(name);
    }

    if (BannedWords.contains(name)) {
      throw Exceptions.format("'{0}' is an invalid name", name);
    }

    if (getManager().getGuild(name) != null) {
      throw Exceptions.format("Name '{0}' is already taken", name);
    }

    // If the guild has a discord channel, then we test if it's been archived
    // by testing if it has '-Archived' in the name, stupid ik
    if (name.contains("-Archived")) {
      throw Exceptions.format(
          "Due to poor planning, '-Archived' may not be "
              + "used in a guild's name"
      );
    }
  }

  /* ----------------------------- GRADIENT ------------------------------- */

  public Component createNameGradient(
      int sections,
      String[] fullName,
      TextColor primary,
      TextColor secondary,
      Style bracket,
      Style text
  ) {
    String complete = String.join("", fullName);
    int length = complete.length();
    var builder = text();

    Pair<TextColor, TextColor> startEndColors = Pair.of(primary, secondary);
    int sectionSize = length / sections;
    int sectionIndex = 0;

    // Iterate through each part of the full name
    for (int i = 0; i < fullName.length; i++) {
      Style style = i == GUILD_NAME ? text : bracket;
      char[] chars = fullName[i].toCharArray();

      // Iterate through each char in the current part of the name
      for (char c : chars) {
        // Increment section index and find color based off of
        // that index
        float progress = (float) sectionIndex / sectionSize;

        ++sectionIndex;
        sectionIndex %= sectionSize;

        TextColor color = TextColor.lerp(
            progress,
            startEndColors.getFirst(),
            startEndColors.getSecond()
        );

        builder.append(text(c, style.color(color)));

        // Section's ended, flip colors so gradient smoothly
        // transitions from the last appended color to new one
        if (sectionIndex == 0) {
          startEndColors = startEndColors.swap();
        }
      }
    }

    return builder.build();
  }
}