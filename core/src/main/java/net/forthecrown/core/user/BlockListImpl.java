package net.forthecrown.core.user;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockListImpl implements UserBlockList {

  @Getter
  private final User user;

  private final Set<UUID> blocked = new ObjectOpenHashSet<>();
  private final Set<UUID> separated = new ObjectOpenHashSet<>();

  public BlockListImpl(User user) {
    this.user = user;
  }

  @Override
  public @NotNull IgnoreResult testIgnored(@NotNull User other) {
    Objects.requireNonNull(other);

    if (separated.contains(other.getUniqueId())) {
      return IgnoreResult.SEPARATED;
    }

    return blocked.contains(other.getUniqueId())
        ? IgnoreResult.BLOCKED
        : IgnoreResult.NOT_IGNORED;
  }

  @Override
  public void setIgnored(@NotNull User other, boolean separated) {
    Objects.requireNonNull(other);
    UUID uuid = other.getUniqueId();

    if (separated) {
      this.separated.add(uuid);
    } else {
      this.blocked.add(uuid);
    }
  }

  @Override
  public void removeIgnored(@NotNull User other) {
    Objects.requireNonNull(other);
    blocked.remove(other.getUniqueId());
  }

  @Override
  public void removeSeparated(@NotNull User other) {
    Objects.requireNonNull(other);
    separated.remove(other.getUniqueId());
  }

  @Override
  public @Nullable JsonElement serialize() {
    if (separated.isEmpty() && blocked.isEmpty()) {
      return null;
    }

    JsonWrapper json = JsonWrapper.create();

    if (!blocked.isEmpty()) {
      json.addList("blocked", blocked, JsonUtils::writeUUID);
    }

    if (!separated.isEmpty()) {
      json.addList("separated", separated, JsonUtils::writeUUID);
    }

    return json.getSource();
  }

  @Override
  public void deserialize(@Nullable JsonElement element) {
    separated.clear();
    blocked.clear();

    if (element == null) {
      return;
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());
    blocked.addAll(json.getList("blocked", JsonUtils::readUUID));
    separated.addAll(json.getList("separated", JsonUtils::readUUID));
  }
}