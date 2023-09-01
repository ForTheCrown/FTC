package net.forthecrown.usables;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.forthecrown.usables.objects.UsableObject;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;

@Getter
@Accessors(fluent = true)
public class Interaction {

  private final Map<String, Object> context = new Object2ObjectOpenHashMap<>();
  private final Player player;
  private final UsableObject object;

  public Interaction(Player player, UsableObject object) {
    this.player = player;
    this.object = object;

    this.object.fillContext(context);
  }

  public static Interaction create(Player player, UsableObject object) {
    return create(player, object, player.hasPermission(UPermissions.ADMIN_INTERACTION));
  }

  public static Interaction create(Player player, UsableObject object, boolean adminInteraction) {
    Interaction interaction = new Interaction(player, object);
    interaction.context.put("adminInteraction", adminInteraction);
    return interaction;
  }

  public User user() {
    return Users.get(player);
  }

  public Optional<Object> getValue(String name) {
    return Optional.ofNullable(context.get(name));
  }

  public <T> Optional<T> getValue(String name, Class<T> type) {
    return getValue(name).filter(type::isInstance).map(o -> (T) o);
  }

  public Optional<Boolean> getBoolean(String key) {
    return getValue(key).map(o -> {
      if (o instanceof Boolean bo) {
        return bo;
      }

      return Boolean.parseBoolean(String.valueOf(o));
    });
  }

  public UUID playerId() {
    return player.getUniqueId();
  }
}
