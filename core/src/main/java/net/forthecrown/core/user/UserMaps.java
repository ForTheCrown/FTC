package net.forthecrown.core.user;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.forthecrown.user.UserLookup.LookupEntry;

public class UserMaps implements Iterable<UserImpl> {

  private final Map<UUID, UserImpl> loaded = new Object2ObjectOpenHashMap<>();
  private final Map<UUID, UserImpl> online = new Object2ObjectOpenHashMap<>();

  private final UserServiceImpl service;

  public UserMaps(UserServiceImpl service) {
    this.service = service;
  }

  public Collection<UserImpl> getOnline() {
    return online.values();
  }

  public Collection<UserImpl> getLoaded() {
    return loaded.values();
  }

  public UserImpl getUser(LookupEntry entry) {
    Objects.requireNonNull(entry);

    UUID uuid = entry.getUniqueId();

    UserImpl user = loaded.computeIfAbsent(uuid, uuid1 -> {
      var res = new UserImpl(service, uuid1);
      service.getStorage().loadUser(res);
      return res;
    });

    if (user.isOnline()) {
      online.put(uuid, user);
    }

    return user;
  }

  public boolean remove(UserImpl user) {
    var removed = loaded.remove(user.getUniqueId());

    if (removed == null) {
      return false;
    }

    online.remove(user.getUniqueId());
    user.service = null;
    return true;
  }

  @Override
  public Iterator<UserImpl> iterator() {
    return loaded.values().iterator();
  }
}