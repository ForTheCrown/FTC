package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;

/**
 * Implementation of {@link Punisher}
 */
public class Punisher extends SerializableObject.Json {

  @Getter
  private final Registry<JailCell> cells = Registries.newRegistry();

  private final Map<UUID, PunishEntry> entryMap = new Object2ObjectOpenHashMap<>();
  private final Map<UUID, JailCell> jailed = new Object2ObjectOpenHashMap<>();

  public Punisher() {
    super(PathUtil.pluginPath("punishments.json"));
  }

  /**
   * Gets an entry for a given UUID
   *
   * @param uuid the UUID to get the entry of
   * @return The UUID's entry, will create an entry if there isn't one already
   */
  @javax.annotation.Nonnull
  public PunishEntry getEntry(UUID uuid) {
    return entryMap.computeIfAbsent(uuid, PunishEntry::new);
  }

  /**
   * Gets an entry for a given UUID
   *
   * @param uuid The UUID to get the entry of
   * @return The UUID's entry, or null, if the UUID doesn't already have an entry
   */
  @javax.annotation.Nullable
  public PunishEntry getNullable(UUID uuid) {
    return entryMap.get(uuid);
  }

  /**
   * Gets a prisoner's jail cell
   *
   * @param prisoner The prisoner's UUID
   * @return The jail the prisoner is in
   */
  @javax.annotation.Nullable
  public JailCell getCell(UUID prisoner) {
    return jailed.get(prisoner);
  }

  /**
   * Sets the given prisoner to be jailed in the given cell
   *
   * @param prisoner The prisoner
   * @param cell     The cell they're in
   */
  public void setJailed(UUID prisoner, JailCell cell) {
    jailed.put(prisoner, cell);
  }

  /**
   * Removes the given prisoner from jail
   *
   * @param prisoner The prisoner to free
   */
  public void removeJailed(UUID prisoner) {
    jailed.remove(prisoner);
  }

  protected void save(JsonWrapper json) {
    JsonWrapper jails = JsonWrapper.create();
    JsonWrapper entries = JsonWrapper.create();

    for (PunishEntry e : entryMap.values()) {
      JsonElement element = e.serialize();

      if (element == null) {
        continue;
      }

      entries.add(e.getHolder().toString(), element);
    }

    for (JailCell c : cells) {
      var keyOptional = cells.getKey(c);

      if (keyOptional.isEmpty()) {
        Loggers.getLogger().warn("Unknown jail found, skipping serialization");
        continue;
      }

      String key = keyOptional.get();
      jails.add(key, c.serialize());
    }

    json.add("entries", entries);
    json.add("jails", jails);
  }

  protected void load(JsonWrapper json) {
    // Clear entries
    for (PunishEntry e : entryMap.values()) {
      e.clearCurrent();
    }

    entryMap.clear();

    // Clear jails
    cells.clear();

    for (Map.Entry<String, JsonElement> e : json.getObject("entries").entrySet()) {
      UUID id = UUID.fromString(e.getKey());

      if (!Users.isPlayerId(id)) {
        Loggers.getLogger().warn("Found invalid ID loading punishments: {}", id);
        continue;
      }

      PunishEntry entry = new PunishEntry(id);
      entry.deserialize(e.getValue());

      entryMap.put(id, entry);
    }

    for (Map.Entry<String, JsonElement> e : json.getObject("jails").entrySet()) {
      String k = e.getKey();
      JailCell cell = JailCell.deserialize(e.getValue());

      cells.register(k, cell);
    }
  }

}