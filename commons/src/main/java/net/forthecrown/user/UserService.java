package net.forthecrown.user;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.forthecrown.registry.Registry;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.name.UserNameFactory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserService {

  /**
   * Gets the user lookup
   * @return User lookup
   */
  UserLookup getLookup();

  /**
   * Gets the username factory. This factory produces user display names and profiles
   * @return Name factory
   */
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
   * Executes a specified {@code operation} on ALL users known to the FTC plugin, both offline and
   * online. The iteration is executed on an async thread
   * <p>
   * Once all users have been iterated through, all offline users are unloaded
   *
   * @param operation Operation to execute
   */
  void executeOnAllUsersAsync(Consumer<User> operation);

  /**
   * Gets the user property registry.
   * <p>
   * This registry WILL be locked before any users are loaded. This is to prevent the removal or
   * registering of any properties that might be required during runtime
   *
   * @return User property registry
   * @see UserProperty User Properties
   */
  Registry<UserProperty<?>> getUserProperties();

  /**
   * Creates a UUID user property builder
   * @return Created builder
   */
  UserProperty.Builder<UUID> createUuidProperty();

  /**
   * Creates a boolean user property builder
   * @return Created builder
   */
  UserProperty.Builder<Boolean> createBooleanProperty();

  /**
   * Creates a text component user property builder
   * @return Created builder
   */
  UserProperty.Builder<Component> createTextProperty();

  /**
   * Creates an enum user property builder
   * @return Created builder
   */
  <E extends Enum<E>> UserProperty.Builder<E> createEnumProperty(Class<E> type);

  /**
   * Gets an immutable collection of all currently online users
   * @return Online users
   */
  Collection<User> getOnlineUsers();

  /**
   * Registers a user component type.
   * <p>
   * The name the component will be registered under and will be used when the user is serialized
   * can be automatically inferred or directly set by placing the {@link ComponentName} annotation
   * on the registered class.
   * <p>
   * If the annotation is not present, then the component's ID string will be inferred. This means
   * if the class name starts with "User" it will be removed, and the class' first character will
   * be converted to lowercase.
   * <br>
   * For example, the class name "UserShopData" will become "shopData"
   * <p>
   * Additionally, the class given here cannot be a synthetic class or an interface class
   *
   * @param componentType Type to register
   */
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