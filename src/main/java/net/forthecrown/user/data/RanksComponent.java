package net.forthecrown.user.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserOfflineException;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

/**
 * Data and functions relating to tiers and titles a user can have.
 *
 * @see UserRank
 * @see RankTier
 * @see #ensureSynced()
 */
public class RanksComponent extends UserComponent {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      KEY_TITLE = "title",
      KEY_AVAILABLE = "titles",
      KEY_TIER = "tier";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */
  /**
   * The user's currently active title
   */
  @Getter
  private UserRank title = UserRanks.DEFAULT;

  /**
   * The user's current tier
   */
  @Getter
  private RankTier tier = RankTier.NONE;

  /**
   * All non-default titles available to this user
   */
  private final Set<UserRank> available = new ObjectOpenHashSet<>();

  /* ----------------------------- CONSTRUCTORS ------------------------------ */

  public RanksComponent(User user, ComponentType<RanksComponent> type) {
    super(user, type);
  }

  /* ----------------------------- GETTERS ------------------------------ */

  /**
   * Gets the cloned set of all non-default titles this user has
   *
   * @return The user's non-default titles
   */
  public Set<UserRank> getAvailable() {
    return new ObjectOpenHashSet<>(available);
  }

  /* ----------------------------- TITLES ------------------------------ */

  /**
   * Tests if this user has the given title
   * <p>
   * If the given title is a 'default' title, then this will call {@link #hasTier(RankTier)} with
   * the given title's tier.
   *
   * @param title The title to test for
   * @return True, if the user has this title
   * @see UserRank#isDefaultTitle()
   */
  public boolean hasTitle(UserRank title) {
    if (title.isDefaultTitle()) {
      return hasTier(title.getTier());
    }

    if (available.contains(title)) {
      return true;
    }

    if (title.getGenderEquivalent() == null) {
      return false;
    }

    return available.contains(title.getGenderEquivalent());
  }

  /**
   * Adds the given title to this user.
   * <p>
   * Delegate for: {@link #addTitle(UserRank, boolean)} with the boolean parameter as true
   *
   * @param title The title to add
   * @see #addTitle(UserRank, boolean)
   * @see #addTitle(UserRank, boolean, boolean)
   */
  public void addTitle(UserRank title) {
    addTitle(title, true);
  }

  /**
   * Adds the given title to this user and potentially changes the user's tier and permissions group
   * if the given title's tier is higher than the current tier.
   * <p>
   * Delegate method for {@link #addTitle(UserRank, boolean, boolean)} with both boolean parameters
   * set to the value of
   * <code>givePermissions</code>
   *
   * @param title           The title to add
   * @param givePermissions True, to change the user's permissions group and tier if the title's
   *                        tier is higher than the current user's tier
   * @see #addTitle(UserRank, boolean, boolean)
   */
  public void addTitle(UserRank title, boolean givePermissions) {
    addTitle(title, givePermissions, givePermissions);
  }

  /**
   * Adds the given title to this user and potentially changes the user's tier and permissions group
   * if the given title's tier is higher than the current tier.
   * <p>
   * The difference between the second and third parameters is that <code>givePermissions</code> is
   * passed onto {@link #setTier(RankTier, boolean)} if the given title has a higher tier than the
   * user's current tier, and
   * <code>setTier</code> determines if the tier check should
   * be done at all.
   *
   * @param title           The title to add
   * @param givePermissions True, to change the user's permissions group and tier if the title's
   *                        tier is higher than the current user's tier
   * @param setTier         True, to test if the user's tier should be changed if it's higher
   */
  public void addTitle(UserRank title, boolean givePermissions, boolean setTier) {
    if (!title.isDefaultTitle()) {
      available.add(title);

      if (title.getGenderEquivalent() != null) {
        available.add(title.getGenderEquivalent());
      }
    }

    if (!hasTier(title.getTier()) && setTier) {
      setTier(title.getTier(), givePermissions);
    }
  }

  /**
   * Removes the given title. If the title is a 'default' title, this method does nothing
   *
   * @param title The title to remove
   */
  public void removeTitle(UserRank title) {
    if (title.isDefaultTitle()) {
      return;
    }

    available.remove(title);

    if (title.getGenderEquivalent() != null) {
      available.remove(title.getGenderEquivalent());
    }
  }

  /**
   * Sets the user's active title.
   * <p>
   * If the user is online, this calls {@link User#updateTabName()}
   *
   * @param title The title to set
   */
  public void setTitle(UserRank title) {
    this.title = title;

    if (!getUser().isOnline()) {
      return;
    }

    getUser().updateTabName();
  }

  /* ----------------------------- TIERS ------------------------------ */

  /**
   * Demotes to the given tier.
   * <p>
   * Changes the user's tier, and title, if it's from a tier higher than the
   * <code>to</code> tier. Removes any extra homes the user may have as a result
   * of the demotion and sets the user's login cosmetic to null
   *
   * @param to The tier to demote to
   */
  public void demote(RankTier to) {
    setTier(to);

    if (title.getTier().ordinal() > to.ordinal()) {
      setTitle(UserRanks.DEFAULT);
    }

    var cosmetics = user.getCosmeticData();
    var login = cosmetics.get(Cosmetics.LOGIN);

    if (login != null && login.getTier().ordinal() > to.ordinal()) {
      cosmetics.set(Cosmetics.LOGIN, null);
    }

    var homes = user.getHomes();
    homes.removeOverMax();
  }

  /**
   * Tests if the user's current tier is equal to or greater than the given tier
   *
   * @param tier The tier to test against
   * @return True, if the user's current tier is higher than or equal to the given tier
   */
  public boolean hasTier(RankTier tier) {
    return getTier().ordinal() >= tier.ordinal();
  }

  /**
   * Adds the given tier to this user.
   * <p>
   * This method works by testing the given tier with {@link #hasTier(RankTier)}. If that returns
   * true, this function does nothing, else it calls {@link #setTier(RankTier)}
   * <p>
   * Exists so that a user's tier is never accidentally assigned to a lower tier by accident and
   * makes the aforementioned possible without having to write this function manually everytime
   *
   * @param tier The tier to add
   * @see #hasTier(RankTier)
   * @see #setTier(RankTier)
   * @see #setTier(RankTier, boolean)
   */
  public void addTier(RankTier tier) {
    if (hasTier(tier)) {
      return;
    }

    setTier(tier);
  }

  /**
   * Delegate method for {@link #setTier(RankTier, boolean)} with the boolean parameter set to true
   *
   * @param tier The tier to set
   * @see #setTier(RankTier, boolean)
   */
  public void setTier(RankTier tier) {
    setTier(tier, true);
  }

  /**
   * Sets this user's tier.
   * <p>
   * If <code>recalculatePermissions</code> is set to true then this method will remove the user's
   * current luck perms group, if it's not the default group, and add the user to the given tier's
   * group, granted it's not the default group.
   *
   * @param tier                   The tier to set
   * @param recalculatePermissions True, to recalculate user's permissions, false to just set the
   *                               tier
   */
  public void setTier(RankTier tier, boolean recalculatePermissions) {
    Objects.requireNonNull(tier);

    if (recalculatePermissions) {
      if (getTier() != RankTier.NONE) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "lp user " + user.getName() + " parent remove " + getTier().getLuckPermsGroup()
        );
      }

      if (tier != RankTier.NONE) {
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "lp user " + user.getName() + " parent add " + tier.getLuckPermsGroup()
        );
      }
    }

    this.tier = tier;
  }

  /**
   * Ensures the user's {@link RankTier} is synced to the user's permission
   * group and that the user's permission group is synced to the user's tier.
   * <p>
   * If the user has a permission group of a higher tier than the one they
   * currently have, then the user's tier is changed. If the user has a tier
   * higher than their luck perms group, then their luck perms group is upgraded
   *
   * @throws UserOfflineException If the user is offline
   */
  public void ensureSynced() throws UserOfflineException {
    user.ensureOnline();

    var values = RankTier.values();
    ArrayUtils.reverse(values);

    for (RankTier tier : values) {
      if (user.hasPermission("group." + tier.getLuckPermsGroup())) {
        if (hasTier(tier)) {
          continue;
        }

        Loggers.getLogger().info("Adding tier {} to {}", tier, user.getName());
        setTier(tier, false);
        return;
      } else if (hasTier(tier)) {
        Loggers.getLogger().info("Adding group {} to {}, due to title/group sync",
            tier.getLuckPermsGroup(), user.getName()
        );

        Util.consoleCommand(
            "lp user %s parent add %s",
            user.getName(), tier.getLuckPermsGroup()
        );
        return;
      }
    }
  }

  /**
   * Clears all available titles, sets the active title to
   * {@link UserRanks#DEFAULT} and the current
   * tier to {@link RankTier#NONE}
   */
  public void clear() {
    available.clear();
    title = UserRanks.DEFAULT;
    tier = RankTier.NONE;
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  @Override
  public void deserialize(@Nullable JsonElement element) {
    clear();

    if (element == null) {
      return;
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());

    tier = json.getEnum(KEY_TIER, RankTier.class, RankTier.NONE);

    if (json.has(KEY_TITLE)) {
      deserializeRank(json.get(KEY_TITLE), rank -> this.title = rank);
    }

    if (json.has(KEY_AVAILABLE)) {
      JsonArray arr = json.getArray(KEY_AVAILABLE);

      for (var e: arr) {
        deserializeRank(e, available::add);
      }
    }
  }

  @Override
  public @Nullable JsonElement serialize() {
    var json = JsonWrapper.create();

    if (tier != RankTier.NONE) {
      json.addEnum(KEY_TIER, tier);
    }

    if (title != UserRanks.DEFAULT) {
      serializeRank(title, primitive -> json.add(KEY_TITLE, primitive));
    }

    if (!available.isEmpty()) {
      JsonArray arr = new JsonArray();

      for (var t: available) {
        serializeRank(t, arr::add);
      }

      if (!arr.isEmpty()) {
        json.add(KEY_AVAILABLE, arr);
      }
    }

    return json.nullIfEmpty();
  }

  private void deserializeRank(JsonElement element,
                               Consumer<UserRank> consumer
  ) {
    UserRanks.REGISTRY.readJson(element)
        .ifPresentOrElse(consumer, () -> {
          LOGGER.warn("Unknown user rank: {}", element);
        });
  }

  private void serializeRank(UserRank rank,
                             Consumer<JsonPrimitive> keyConsumer
  ) {
    UserRanks.REGISTRY.writeJson(rank)
        .ifPresentOrElse(keyConsumer, () -> {
          LOGGER.warn("Unregistered rank found {}, cannot serialize", rank);
        });
  }
}