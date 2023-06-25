package net.forthecrown.user;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.UserProperty.Builder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserService {

  UserLookup getLookup();

  UserNameFactory getNameFactory();

  /**
   * Begins an async load of ALL users known to the FTC plugin
   * <p>
   * It is more suggestible to use {@link #executeOnAllUsers(Consumer)} for performing operations
   * on all known users
   *
   * @return A future that will be completed via error, if any user(s) fail to load, or will be
   *         completed once all users have been loaded
   */
  CompletableFuture<Collection<User>> loadAllUsers();

  /**
   * Executes a specified {@code operation} on ALL users known to the FTC plugin, both offline and
   * online.
   * <p>
   * Once all users have been iterated through, all offline users are unloaded
   * <p>
   * <b>Note:</b> This method will run on the thread the method is called on, no async execution
   * is performed, for async user loading see {@link #loadAllUsers()}
   *
   * @param operation Operation to perform
   */
  void executeOnAllUsers(Consumer<User> operation);

  /**
   * Gets the user property registry.
   * <p>
   * This registry WILL be locked before any users are loaded. This is to prevent the removal or
   * registering of any properties that might be required during runtime
   *
   * @return User property registry
   */
  Registry<UserProperty<?>> getUserProperties();

  Builder<UUID> createUuidProperty();

  UserProperty.Builder<Boolean> createBooleanProperty();

  UserProperty.Builder<Component> createTextProperty();

  <E extends Enum<E>> UserProperty.Builder<E> createEnumProperty(Class<E> type);

  void registerComponent(Class<? extends UserComponent> componentType);

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