package net.forthecrown.user;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface User extends ForwardingAudience.Single {

  /**
   * Gets the user's UUID.
   * <p>
   * The returned UUID is the same UUID as returned by {@link Player#getUniqueId()} for this user's
   * underlying player profile, which can be accessed with {@link #getPlayer()},
   * {@link #getOfflinePlayer()} or {@link #getProfile()}
   *
   * @return User's UUID
   */
  UUID getUniqueId();

  @Override
  default @NotNull Audience audience() {
    return isOnline() ? getPlayer() : Audience.empty();
  }

  /**
   * Gets the user's Bukkit {@link Player} object
   *
   * @return The user's player object, null, if not online
   */
  Player getPlayer();

  /**
   * Gets the user's {@link OfflinePlayer} object.
   *
   * @return The user's offline player object
   */
  OfflinePlayer getOfflinePlayer();

  /**
   * Tests if the user is online
   *
   * @return True, if {@link #getPlayer()} != null
   */
  boolean isOnline();

  /**
   * Ensures the user is online.
   * <p>
   * If the user is not online, this method will throw a
   * {@link UserOfflineException} to prevent any online-only code from being
   * executed.
   *
   * @throws UserOfflineException If the user is not online
   */
  void ensureOnline() throws UserOfflineException;

  /**
   * Gets the user's name
   *
   * @return The user's name
   */
  String getName();

  /**
   * Gets a user's IP address
   * @return IP address, or null, if IP is unknown
   */
  @Nullable
  String getIp();

  /**
   * Gets the last name a user had while online.
   * <p>
   * May return the same value as {@link #getName()}
   *
   * @return User's last online name
   */
  String getLastOnlineName();

  /**
   * Gets a list of a user's previous names
   * @return Previous names
   */
  List<String> getPreviousNames();

  /**
   * Gets the profile of this user
   * <p>
   * Note: It's just a new craft player profile that has no properties or
   * anything. Do {@link PlayerProfile#complete()} for those
   *
   * @return The user's profile
   */
  PlayerProfile getProfile();

  /**
   * Gets the user's location. If the user is offline, the location the user logged out at will be
   * returned instead.
   *
   * @return The user's location, if offline, then it's the user's last known
   * location.
   */
  Location getLocation();

  /**
   * Gets the user's inventory, only works online since this is essentially a delegate for
   * {@link #getPlayer()} and {@link Player#getInventory()}
   *
   * @return User's inventory
   * @throws UserOfflineException If the user is not online
   */
  Inventory getInventory() throws UserOfflineException;

  /**
   * Gets the world the user is currently in.
   * <p>
   * If the user is offline, returns the world in which the user logged out in
   *
   * @return The user's world
   */
  World getWorld();

  /**
   * Gets the command source object for this user
   *
   * @return The {@link CommandSource} that represents the player of this user
   * object
   * @throws UserOfflineException If the user is not online
   */
  CommandSource getCommandSource() throws UserOfflineException;

  /**
   * Gets the user's return location. This is the location the user should
   * return to when they use /back. As such, it should be updated whenever the
   * user TPAs, goes home or uses a region pole
   *
   * @return The user's return location
   */
  Location getReturnLocation();

  /**
   * Sets the location the user will return to when they execute the {@code /back} command
   * @param location New return location, use {@code null} to remove return location
   */
  void setReturnLocation(@Nullable Location location);

  void playSound(Sound uiButtonClick, float volume, float pitch);

  /* ----------------------------- COMPONENTS ------------------------------ */

  <T extends UserComponent> T getComponent(Class<T> componentType);

  /* ----------------------------- TIME DATA ------------------------------ */

  /**
   * Gets a time field's value
   * @param field Field to get the value of
   * @return Specified field's value, or {@code -1}, if unset
   */
  long getTime(TimeField field);

  /**
   * Sets a time field's value
   * @param field Field to set
   * @param value Value to set the field to, use {@code -1}, to unset
   */
  void setTime(TimeField field, long value);

  /**
   * Delegate for {@link #setTime(TimeField, long)} which uses {@link System#currentTimeMillis()}
   * @param field Field to set
   */
  default void setTimeToNow(TimeField field) {
    setTime(field, System.currentTimeMillis());
  }

  /* ----------------------------- DISPLAY INFO ------------------------------ */

  default Component displayName() {
    return displayName(null);
  }

  default Component displayName(@Nullable Audience viewer) {
    return displayName(viewer, true);
  }

  Component displayName(@Nullable Audience viewer, boolean useNickname);

  Component nickOrName();

  Component name();

  Component nickname();

  String getNickname();

  void setNickname(String nickname);

  /* ----------------------------- AFKING ------------------------------ */

  /**
   * Sets the user's AFK state
   * @param afk AFK state
   * @param reason Reason for the user being AFK, will be displayed when viewing {@code /profile}
   *               for this user
   *
   * @throws UserOfflineException If the user is not online
   */
  void setAfk(boolean afk, @Nullable Component reason) throws UserOfflineException;

  /**
   * Gets the amount of time this user has been in an AFK state since logging in
   * @return User's current AFK time
   */
  long getAfkTime();

  /**
   * Makes this user enter an AFK state
   *
   * @param reason The reason the user is entering the AFK state
   * @throws IllegalStateException If the user is already AFK
   * @throws UserOfflineException  If the user is not AFK
   */
  void afk(@Nullable Component reason) throws IllegalStateException, UserOfflineException;

  /**
   * Makes this user leave their AFK state
   *
   * @throws IllegalStateException If the user is not AFK
   * @throws UserOfflineException  If the user is not online
   */
  void unafk() throws IllegalStateException, UserOfflineException;

  /**
   * Tests if this user is currently set as AFK
   * @return {@code true}, if the user is AFK, {@code false} otherwise
   */
  boolean isAfk();

  /* ----------------------------- CURRENCIES ------------------------------ */

  int getGems();

  void setGems(int gems);

  void addGems(int gems);

  void removeGems(int gems);

  int getBalance();

  void setBalance(int balance);

  void addBalance(int balance);

  void removeBalance(int balance);

  /* ----------------------------- PROPERTY ACCESS ------------------------------ */

  <T> boolean has(UserProperty<T> property);

  <T> T get(UserProperty<T> property);

  <T> void set(UserProperty<T> property, T value);

  default boolean flip(UserProperty<Boolean> property) {
    boolean state = get(property);
    set(property, !state);
    return state;
  }

  /* ----------------------------- PERMISSIONS ------------------------------ */

  boolean hasPermission(String permission);

  boolean hasPermission(Permission permission);

  /* ----------------------------- UPDATE METHODS ------------------------------ */

  void updateVanished();

  void updateGodMode();

  void updateFlying();

  void updateTabName();

  /* ----------------------------- TELEPORTING ------------------------------ */

  /**
   * Tests if a user can teleport. This function will also tell the user when
   * they can teleport again.
   *
   * @return True, if the user can teleport, false otherwise
   */
  boolean checkTeleporting();

  /**
   * Creates a {@link UserTeleport} to the given destination.
   * <p>
   * Note: The teleport itself won't start until {@link UserTeleport#start()} is
   * called.
   *
   * @param destination The destination supplier, see:
   *                    {@link UserTeleport#getDestination()}
   * @param type        The teleprot type
   * @return The created teleport
   * @throws UserOfflineException If the user isn't online
   */
  UserTeleport createTeleport(Supplier<Location> destination, UserTeleport.Type type)
      throws UserOfflineException;

  /**
   * Tests if the user has a currently active teleport.
   *
   * @return True, if the user has an active teleport
   */
  boolean isTeleporting();

  /**
   * Tests if the user can teleport. If {@link #isTeleporting()} returns true,
   * this method will return false. Otherwise, this method will return if the
   * {@link TimeField#NEXT_TELEPORT} is in the past or is unset.
   *
   * @return True if the user can teleport, false otherwise
   */
  boolean canTeleport();
}