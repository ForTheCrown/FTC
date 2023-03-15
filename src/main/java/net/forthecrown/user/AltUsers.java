package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializableObject;
import org.bukkit.entity.Player;

public class AltUsers extends SerializableObject.Json {

  private final Map<UUID, UUID> alt2Main = new Object2ObjectOpenHashMap<>();

  public AltUsers(Path filePath) {
    super(filePath);
  }

  public void save(JsonWrapper json) {
    for (var e : alt2Main.entrySet()) {
      json.add(
          e.getKey().toString(),
          e.getValue().toString()
      );
    }
  }

  public void load(JsonWrapper json) {
    alt2Main.clear();

    for (var e : json.entrySet()) {
      addEntry(
          UUID.fromString(e.getKey()),
          UUID.fromString(e.getValue().getAsString())
      );
    }
  }

  public UUID getMain(UUID id) {
    return alt2Main.get(id);
  }

  public boolean isAlt(UUID id) {
    return alt2Main.containsKey(id);
  }

  public Collection<UUID> getOtherAccounts(UUID uuid) {
    Set<UUID> alts = new ObjectOpenHashSet<>(getAlts(uuid));
    var main = getMain(uuid);

    if (main != null) {
      alts.addAll(getAlts(main));
      alts.add(main);
    }

    return alts;
  }

  public boolean isAltForAny(UUID id, Collection<Player> players) {
    UUID main = getMain(id);

    if (main == null) {
      return false;
    }

    for (Player p : players) {
      if (main.equals(p.getUniqueId())) {
        return true;
      }
    }

    return false;
  }

  public List<UUID> getAlts(UUID main) {
    List<UUID> list = new ObjectArrayList<>();

    for (Map.Entry<UUID, UUID> entry : alt2Main.entrySet()) {
      if (!entry.getValue().equals(main)) {
        continue;
      }

      list.add(entry.getKey());
    }
    return list;
  }

  public void addEntry(UUID alt, UUID main) {
    alt2Main.put(alt, main);
  }

  public void removeEntry(UUID alt) {
    alt2Main.remove(alt);
  }
}