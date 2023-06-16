package net.forthecrown.titles;


import static net.forthecrown.titles.UserRanks.DEFAULT;
import static net.forthecrown.titles.UserRanks.DEFAULT_NAME;
import static net.forthecrown.titles.UserRanks.DEFAULT_REF;
import static net.forthecrown.titles.UserRanks.REGISTRY;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.registry.Ref;
import net.forthecrown.registry.Ref.KeyRef;
import net.forthecrown.registry.Registries;
import net.forthecrown.titles.events.TierChangeEvent;
import net.forthecrown.user.ComponentName;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.user.UserOfflineException;
import net.forthecrown.utils.TransformingSet;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Data and functions relating to tiers and titles a user can have.
 *
 * @see UserRank
 * @see RankTier
 * @see #ensureSynced()
 */
@ComponentName("rankData")
public class UserTitles implements UserComponent {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      KEY_TITLE = "title",
      KEY_AVAILABLE = "titles",
      KEY_TIER = "tier";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  private final User user;

  /**
   * The user's currently active title
   */
  private KeyRef<UserRank> title = DEFAULT_REF;

  /**
   * The user's current tier
   */
  @Getter
  private RankTier tier = RankTier.NONE;

  /**
   * All non-default titles available to this user
   */
  private final TransformingSet<String, UserRank> available
      = Registries.keyBackedSet(REGISTRY);

  /* ----------------------------- CONSTRUCTORS ------------------------------ */

  public UserTitles(User user) {
    this.user = user;
  }

  /* ----------------------------- GETTERS ------------------------------ */

  /**
   * Gets the cloned set of all non-default titles this user has
   *
   * @return The user's non-default titles
   */
  public Set<UserRank> getAvailable() {
    return available;
  }

  public UserRank getTitle() {
    return title.orElse(REGISTRY, DEFAULT);
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

    var genderEquivalent = title.getGenderEquivalent();
    if (genderEquivalent == null) {
      return false;
    }

    return available.contains(genderEquivalent);
  }

  /**
   * Adds the given title to this user.
   * <p>
   * Delegate for: {@link #addTitle(UserRank, boolean)} with the boolean parameter as true
   *
   * @param title The title to add
   * @see #addTitle(UserRank, boolean)
   */
  public void addTitle(UserRank title) {
    addTitle(title, true);
  }

  /**
   * Adds the given title to this user and potentially changes the user's tier and permissions group
   * if the given title's tier is higher than the current tier.
   * <p>
   * The difference between the second and third parameters is that <code>givePermissions</code> is
   * passed onto {@link #setTier(RankTier)} if the given title has a higher tier than the
   * user's current tier, and
   * <code>setTier</code> determines if the tier check should
   * be done at all.
   *
   * @param title           The title to add
   * @param setTier         True, to test if the user's tier should be changed if it's higher
   */
  public void addTitle(UserRank title, boolean setTier) {
    if (!title.isDefaultTitle()) {
      available.add(title);

      if (title.getGenderEquivalent() != null) {
        available.add(title.getGenderEquivalent());
      }
    }

    if (!hasTier(title.getTier()) && setTier) {
      setTier(title.getTier());
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

    //recalculateLoginEffect();
  }

  /**
   * Sets the user's active title.
   * <p>
   * If the user is online, this calls {@link User#updateTabName()}
   *
   * @param title The title to set
   */
  public void setTitle(UserRank title) {
    var titleKey = REGISTRY.getKey(title).orElseThrow();
    this.title = Ref.key(titleKey);

    if (!user.isOnline()) {
      return;
    }

    user.updateTabName();
  }

  /* ----------------------------- TIERS ------------------------------ */

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
   */
  public void addTier(RankTier tier) {
    if (hasTier(tier)) {
      return;
    }

    setTier(tier);
  }

  /**
   * Sets this user's tier.
   * <p>
   * If <code>recalculatePermissions</code> is set to true then this method will remove the user's
   * current luck perms group, if it's not the default group, and add the user to the given tier's
   * group, granted it's not the default group.
   *
   * @param tier                   The tier to set
   */
  public void setTier(RankTier tier) {
    Objects.requireNonNull(tier);

    if (this.tier == tier) {
      return;
    }

    if (getTier() != RankTier.NONE) {
      Commands.executeConsole("lp user %s parent remove %s",
          user.getName(), getTier().getLuckPermsGroup()
      );
    }

    if (tier != RankTier.NONE) {
      Commands.executeConsole("lp user %s parent add %s",
          user.getName(), tier.getLuckPermsGroup()
      );
    }

    var title = getTitle();
    if (title.getTier().ordinal() > tier.ordinal()) {
      setTitle(DEFAULT);
    }

    TierChangeEvent event = new TierChangeEvent(user, this.tier, tier);
    event.callEvent();

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
/*
    user.getDiscordMember().ifPresent(member -> {
      OffsetDateTime boostStart = member.getTimeBoosted();
      Optional<UserRank> boostTitle = REGISTRY.get("booster");

      if (boostTitle.isEmpty()) {
        return;
      }

      if (boostStart == null) {
        removeTitle(boostTitle.get());
      } else {
        addTitle(boostTitle.get());
      }
    });*/

    var values = RankTier.values();
    ArrayUtils.reverse(values);

    for (RankTier tier : values) {
      if (user.hasPermission("group." + tier.getLuckPermsGroup())) {
        if (hasTier(tier)) {
          continue;
        }

        LOGGER.info("Adding tier {} to {}", tier, user.getName());
        this.tier = tier;

        return;
      } else if (hasTier(tier)) {
        LOGGER.info("Adding group {} to {}, due to title/group sync",
            tier.getLuckPermsGroup(), user.getName()
        );

        Commands.executeConsole(
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
    title = DEFAULT_REF;
    tier = RankTier.NONE;
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public void deserialize(@Nullable JsonElement element) {
    clear();

    if (element == null) {
      return;
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());

    tier = json.getEnum(KEY_TIER, RankTier.class, RankTier.NONE);

    if (json.has(KEY_TITLE)) {
      this.title = Ref.key(json.getString(KEY_TITLE));
    }

    if (json.has(KEY_AVAILABLE)) {
      JsonArray arr = json.getArray(KEY_AVAILABLE);

      JsonUtils.stream(arr)
          .map(JsonElement::getAsString)
          .forEach(s -> available.getBackingSet().add(s));
    }
  }

  public @Nullable JsonElement serialize() {
    var json = JsonWrapper.create();

    if (tier != RankTier.NONE) {
      json.addEnum(KEY_TIER, tier);
    }

    if (!title.key().equalsIgnoreCase(DEFAULT_NAME)) {
      json.add(KEY_TITLE, title.key());
    }

    if (!available.isEmpty()) {
      JsonArray arr = JsonUtils.ofStream(
          available.getBackingSet()
              .stream()
              .map(JsonPrimitive::new)
      );

      if (!arr.isEmpty()) {
        json.add(KEY_AVAILABLE, arr);
      }
    }

    return json.nullIfEmpty();
  }
}