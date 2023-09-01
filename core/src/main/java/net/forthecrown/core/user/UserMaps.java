package net.forthecrown.core.user;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.UserLookup.LookupEntry;

import java.util.*;
import org.slf4j.Logger;

public class UserMaps implements Iterable<UserImpl> {

  private static final Logger LOGGER = Loggers.getLogger();

  private final Map<UUID, UserImpl> loaded = new Object2ObjectOpenHashMap<>(100);
  private final Map<UUID, UserImpl> online = new Object2ObjectOpenHashMap<>(100);

  private final UserServiceImpl service;

  @Getter @Setter
  private boolean loadingEnabled = false;

  @Getter @Setter
  private boolean userAutoUnloadingEnabled = true;

  public UserMaps(UserServiceImpl service) {
    this.service = service;
  }

  public Collection<UserImpl> getOnline() {
    return online.values();
  }

  public Collection<UserImpl> getLoaded() {
    return loaded.values();
  }

  public synchronized UserImpl getUser(LookupEntry entry) {
    Objects.requireNonNull(entry);

    Preconditions.checkState(
        loadingEnabled,
        "User loading not yet enabled (User load attempted at startup before "
            + "user service was fully initialized)"
    );

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

  public synchronized boolean remove(UserImpl user) {
    var removed = loaded.remove(user.getUniqueId());
    online.remove(user.getUniqueId());

    if (removed == null) {
      return false;
    }

    user.setTimeToNow(TimeField.LAST_LOADED);
    service.getStorage().saveUser(user);
    user.service = null;
    user.online = false;
    user.player = null;

    return true;
  }

  @Override
  public Iterator<UserImpl> iterator() {
    return loaded.values().iterator();
  }
}