package net.forthecrown.user;

import java.util.Collection;
import java.util.UUID;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserService {

  UserLookup getLookup();

  UserNameFactory getNameFactory();

  /**
   * Gets the user property registry.
   * <p>
   * This registry WILL be locked before any users are loaded. This is to prevent the removal or
   * registering of any properties that might be required during runtime
   *
   * @return User property registry
   */
  Registry<UserProperty<?>> getUserProperties();

  UserProperty.Builder<Boolean> createBooleanProperty();

  UserProperty.Builder<Component> createTextProperty();

  <E extends Enum<E>> UserProperty.Builder<E> createEnumProperty(Class<E> type);

  void registerComponentType(Class<? extends UserComponent> componentType);

  @NotNull
  User getUser(@NotNull LookupEntry entry);

  @Nullable
  UUID getMainAccount(@NotNull UUID altPlayerId);

  boolean isAltAccount(@NotNull UUID playerId);

  Collection<UUID> getOtherAccounts(@NotNull UUID playerId);

  boolean isAltForAny(@NotNull UUID playerId, @NotNull Collection<Player> players);

  void setAltAccount(@NotNull UUID altPlayerId, @NotNull UUID mainPlayerId);

  void removeAltAccount(@NotNull UUID altPlayerId);
}