package net.forthecrown.core;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.forthecrown.Cooldowns;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CooldownsImpl implements Cooldowns {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final CooldownsImpl cooldowns = new CooldownsImpl();

  @Getter
  private final Path path;

  private final Map<String, CategoryMap> cooldownMap
      = new Object2ObjectOpenHashMap<>();

  private CooldownsImpl() {
    this.path = PathUtil.pluginPath("cooldowns.json");
  }

  public void clear() {
    cooldownMap.clear();
  }

  public boolean isEmpty() {
    return cooldownMap.isEmpty();
  }

  private void dropExpiredEntries() {
    cooldownMap.values().removeIf(categoryMap -> {
      categoryMap.dropExpiredEntries();
      return categoryMap.isEmpty();
    });
  }

  @Override
  public Set<String> getExistingCategories() {
    return Collections.unmodifiableSet(cooldownMap.keySet());
  }

  /* ----------------------------- QUERYING ------------------------------- */

  @Override
  public void cooldown(UUID uuid, String category, Duration time) {
    Objects.requireNonNull(category);
    Objects.requireNonNull(uuid);
    Objects.requireNonNull(time);

    long timeMillis = time.toMillis();

    if (timeMillis == 0) {
      return;
    }

    CategoryMap map = cooldownMap.computeIfAbsent(category, s -> new CategoryMap());

    long endTime = timeMillis == NO_END_COOLDOWN
        ? NO_END_COOLDOWN
        : System.currentTimeMillis() + timeMillis;

    map.put(uuid, endTime);
  }

  @Override
  public boolean remove(UUID playerId, String category) {
    Objects.requireNonNull(playerId);
    Objects.requireNonNull(category);

    CategoryMap map = cooldownMap.get(category);

    if (map == null || map.isEmpty() || !map.containsKey(playerId)) {
      return false;
    }

    map.removeLong(playerId);
    dropExpiredEntries();

    return true;
  }

  @Override
  public boolean onCooldown(UUID uuid, String category) {
    Objects.requireNonNull(category);
    Objects.requireNonNull(uuid);

    CategoryMap map = cooldownMap.get(category);

    if (map == null || map.isEmpty() || !map.containsKey(uuid)) {
      return false;
    }

    long end = map.getLong(uuid);

    if (end == NO_END_COOLDOWN) {
      return true;
    }

    if (Time.isPast(end)) {
      dropExpiredEntries();
      return false;
    }

    return true;
  }

  @Override
  public @Nullable Duration getRemainingTime(UUID uuid, String category) {
    Objects.requireNonNull(category);
    Objects.requireNonNull(uuid);

    CategoryMap map = cooldownMap.get(category);

    if (map == null || map.isEmpty() || !map.containsKey(uuid)) {
      return null;
    }

    long endTime = map.getLong(uuid);

    if (endTime == NO_END_COOLDOWN) {
      return Duration.ofSeconds(-1);
    }

    long until = Time.timeUntil(endTime);

    if (until <= 0) {
      dropExpiredEntries();
      return null;
    }

    return Duration.ofMillis(until);
  }

  /**
   * Same as {@link #containsOrAdd(UUID, String, long)} with
   * {@link #TRANSIENT_CATEGORY}
   * @see #containsOrAdd(UUID, String, long)
   */
  public boolean containsOrAdd(UUID uuid, long timeMillis) {
    return containsOrAdd(uuid, TRANSIENT_CATEGORY, timeMillis);
  }

  /**
   * Tests if a player is on cooldown in a category, if they are, returns true,
   * else, adds the player to the category's cooldown list and returns false.
   *
   * @param uuid       The player to test or place in cooldown
   * @param category   The cooldown category
   * @param timeMillis The time in millis, the player should be placed into
   *                   cooldown, {@link #NO_END_COOLDOWN} for never-ending
   *                   cooldown
   *
   * @return True, if the player was already on cooldown, false, if they were
   *         added just now
   */
  public boolean containsOrAdd(UUID uuid, String category, long timeMillis) {
    if (onCooldown(uuid, category))  {
      return true;
    }

    cooldown(uuid, category, timeMillis);
    return false;
  }

  /**
   * Same as {@link #testAndThrow(UUID, String, long)}, with
   * {@link #TRANSIENT_CATEGORY}
   * @see #testAndThrow(UUID, String, long)
   */
  public void testAndThrow(UUID uuid, long timeMillis)
      throws CommandSyntaxException
  {
    testAndThrow(uuid, TRANSIENT_CATEGORY, timeMillis);
  }

  /**
   * Uses {@link #containsOrAdd(UUID, String, long)} to determine if the player
   * is on cooldown, if they are, throws a syntax exception, else does nothing.
   *
   * @param uuid       The player's UUID
   * @param category   The name of the category
   * @param timeMillis The time in millis, the player should be placed into
   *                   cooldown, {@link #NO_END_COOLDOWN} for never-ending
   *                   cooldown
   *
   * @throws CommandSyntaxException If the player was on cooldown. Uses
   * {@link Exceptions#onCooldown(long)} if the timeMillis is not
   * {@link #NO_END_COOLDOWN}, else just says 'This can only be done once'
   */
  public void testAndThrow(UUID uuid, String category, long timeMillis)
      throws CommandSyntaxException
  {
    if (!containsOrAdd(uuid, category, timeMillis)) {
      return;
    }

    if (timeMillis == NO_END_COOLDOWN) {
      throw Exceptions.format("This could only be done once");
    }

    if (timeMillis > TimeUnit.MINUTES.toMillis(10)) {
      Duration remaining = getRemainingTime(uuid, category);
      throw Exceptions.format("You can do this in {0, time}", remaining);
    }

    throw Exceptions.onCooldown(timeMillis);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load() {
    clear();

    SerializationHelper.readJsonFile(getPath(), wrapper -> {
      wrapper.entrySet().forEach(entry -> {
        String name = entry.getKey();
        var val = entry.getValue();

        if (!val.isJsonObject()) {
          LOGGER.error("Found non-object entry in {}: {}", getPath(), name);
          return;
        }

        JsonWrapper json = JsonWrapper.wrap(val.getAsJsonObject());
        CategoryMap map = new CategoryMap();
        map.load(json);

        if (!map.isEmpty()) {
          cooldownMap.put(name, map);
        }
      });
    });
  }

  public void save() {
    dropExpiredEntries();

    SerializationHelper.writeJsonFile(getPath(), wrapper -> {
      if (isEmpty()) {
        return;
      }

      cooldownMap.forEach((s, categoryMap) -> {
        if (s.equals(TRANSIENT_CATEGORY)) {
          return;
        }

        JsonWrapper categoryJson = JsonWrapper.create();
        categoryMap.save(categoryJson);
        wrapper.add(s, categoryJson);
      });
    });
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  private static class CategoryMap extends Object2LongOpenHashMap<UUID> {

    void dropExpiredEntries() {
      long time = System.currentTimeMillis();

      object2LongEntrySet().removeIf(uuidEntry -> {
        long ends = uuidEntry.getLongValue();

        if (ends == NO_END_COOLDOWN) {
          return false;
        }

        return time >= ends;
      });
    }

    void save(JsonWrapper json) {
      long currentTime = System.currentTimeMillis();

      forEach((uuid, cooldownEnd) -> {
        if (cooldownEnd != NO_END_COOLDOWN && cooldownEnd <= currentTime) {
          return;
        }

        json.add(uuid.toString(), cooldownEnd);
      });
    }

    void load(JsonWrapper json) {
      long currentTime = System.currentTimeMillis();

      json.entrySet().forEach(entry -> {
        UUID uuid;

        try {
          uuid = UUID.fromString(entry.getKey());
        } catch (IllegalArgumentException exc) {
          LOGGER.error("Found non UUID in cooldowns {}", entry, exc);
          return;
        }

        long ends = entry.getValue().getAsLong();

        if (ends != NO_END_COOLDOWN && ends <= currentTime) {
          return;
        }

        put(uuid, ends);
      });
    }
  }
}