package net.forthecrown.core.commands.tpa;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.user.User;

public class TeleportRequests {

  private static final Map<UUID, RequestEntry> entryMap = new HashMap<>();

  public static void add(TeleportRequest request) {
    var senderEntry = getOrCreateEntry(request.getSender());
    var targetEntry = getOrCreateEntry(request.getTarget());

    senderEntry.outgoing.add(request);
    targetEntry.incoming.add(request);
  }

  public static void remove(TeleportRequest request) {
    removeRequest(request.getTarget(), true, request);
    removeRequest(request.getSender(), false, request);
  }

  private static void removeRequest(User user, boolean incoming, TeleportRequest request) {
    RequestEntry entry = entryMap.get(user.getUniqueId());

    if (entry == null) {
      return;
    }

    if (incoming) {
      entry.incoming.remove(request);
    } else {
      entry.outgoing.remove(request);
    }

    if (entry.isEmpty()) {
      entryMap.remove(user.getUniqueId());
    }
  }

  private static RequestEntry getOrCreateEntry(User user) {
    return entryMap.computeIfAbsent(user.getUniqueId(), uuid -> new RequestEntry());
  }

  public static TeleportRequest getOutgoing(User sender, User target) {
    return entry(sender).map(e -> e.getOutgoing(target)).orElse(null);
  }

  public static TeleportRequest getIncoming(User target, User sender) {
    return entry(target).map(e -> e.getIncoming(sender)).orElse(null);
  }

  public static boolean clearIncoming(User target) {
    return entry(target)
        .map(entry -> {
          if (entry.incoming.isEmpty()) {
            return false;
          }

          entry.incoming.forEach(TeleportRequest::stop);
          return true;
        })
        .orElse(false);
  }

  public static TeleportRequest latestIncoming(User target) {
    return entry(target).map(RequestEntry::latestIncoming).orElse(null);
  }

  public static TeleportRequest latestOutgoing(User sender) {
    return entry(sender).map(RequestEntry::latestOutgoing).orElse(null);
  }

  private static Optional<RequestEntry> entry(User user) {
    return Optional.ofNullable(entryMap.get(user.getUniqueId()));
  }

  public static class RequestEntry {
    private final List<TeleportRequest> incoming = new ReferenceArrayList<>();
    private final List<TeleportRequest> outgoing = new ReferenceArrayList<>();

    public TeleportRequest latestIncoming() {
      return getLatest(incoming);
    }

    public TeleportRequest latestOutgoing() {
      return getLatest(outgoing);
    }

    private static TeleportRequest getLatest(List<TeleportRequest> requests) {
      if (requests.isEmpty()) {
        return null;
      }

      return requests.get(requests.size() - 1);
    }

    public TeleportRequest getIncoming(User sender) {
      for (var i: incoming) {
        if (i.getSender().equals(sender)) {
          return i;
        }
      }

      return null;
    }

    public TeleportRequest getOutgoing(User target) {
      for (var i: incoming) {
        if (i.getTarget().equals(target)) {
          return i;
        }
      }

      return null;
    }

    boolean isEmpty() {
      return outgoing.isEmpty() && incoming.isEmpty();
    }
  }
}
