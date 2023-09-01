package net.forthecrown.command.request;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.User;
import net.kyori.adventure.audience.Audience;

public class RequestTable<R extends PlayerRequest> {

  private final Map<UUID, TableEntry> entryMap = new HashMap<>();

  @Getter @Setter
  private RequestValidator<R> validator;

  public void sendRequest(R request) throws CommandSyntaxException {
    validate(request, request.getSender());

    request.start();
    request.onBegin();

    add(request);
  }

  public void validate(R request, Audience viewer) throws CommandSyntaxException {
    if (validator == null) {
      return;
    }

    validator.validate(request, viewer);
  }

  public void add(R request) {
    var senderEntry = getOrCreateEntry(request.getSenderId());
    var targetEntry = getOrCreateEntry(request.getTargetId());

    senderEntry.outgoing.add(request);
    targetEntry.incoming.add(request);

    request.table = this;
  }

  public void remove(R request) {
    removeRequest(request.getTargetId(), true, request);
    removeRequest(request.getSenderId(), false, request);

    request.table = null;
  }

  private TableEntry getOrCreateEntry(UUID user) {
    return entryMap.computeIfAbsent(user, uuid -> new TableEntry());
  }

  private void removeRequest(UUID user, boolean incoming, R request) {
    TableEntry entry = entryMap.get(user);

    if (entry == null) {
      return;
    }

    if (incoming) {
      entry.incoming.remove(request);
    } else {
      entry.outgoing.remove(request);
    }

    if (entry.isEmpty()) {
      entryMap.remove(user);
    }
  }

  public R getOutgoing(User sender, User target) {
    return entry(sender).map(e -> e.getOutgoing(target)).orElse(null);
  }

  public R getIncoming(User target, User sender) {
    return entry(target).map(e -> e.getIncoming(sender)).orElse(null);
  }

  public boolean clearIncoming(User target) {
    return entry(target)
        .map(entry -> {
          if (entry.incoming.isEmpty()) {
            return false;
          }

          entry.incoming.forEach(PlayerRequest::stop);
          return true;
        })
        .orElse(false);
  }

  public R latestIncoming(User target) {
    return entry(target).map(TableEntry::latestIncoming).orElse(null);
  }

  public R latestOutgoing(User sender) {
    return entry(sender).map(TableEntry::latestOutgoing).orElse(null);
  }

  private Optional<TableEntry> entry(User user) {
    return Optional.ofNullable(entryMap.get(user.getUniqueId()));
  }

  private class TableEntry {
    private final List<R> incoming = new ReferenceArrayList<>();
    private final List<R> outgoing = new ReferenceArrayList<>();

    public R latestIncoming() {
      return getLatest(incoming);
    }

    public R latestOutgoing() {
      return getLatest(outgoing);
    }

    private static <R> R getLatest(List<R> requests) {
      if (requests.isEmpty()) {
        return null;
      }

      return requests.get(requests.size() - 1);
    }

    public R getIncoming(User sender) {
      for (var i: incoming) {
        if (i.getSenderId().equals(sender.getUniqueId())) {
          return i;
        }
      }

      return null;
    }

    public R getOutgoing(User target) {
      for (var i: incoming) {
        if (i.getTargetId().equals(target.getUniqueId())) {
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
