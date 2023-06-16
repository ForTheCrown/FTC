package net.forthecrown.user;

import lombok.Getter;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;

@Getter
public final class TimeField {

  public static final Registry<TimeField> REGISTRY = Registries.newRegistry();

  /**
   * Time stamp of when the user is next allowed to use a command like /home or /tpa
   */
  public static final TimeField NEXT_TELEPORT = transientField("nextAllowedTeleport");

  /**
   * The first time the player joined this server
   */
  public static final TimeField FIRST_JOIN = field("firstJoined");

  /**
   * The last time the user was loaded for any reason
   */
  public static final TimeField LAST_LOADED = field("lastLoad");

  /**
   * The last time the user logged in to the server
   */
  public static final TimeField LAST_LOGIN = field("lastJoin");

  /**
   * The last time the player went AFK, not serialized
   */
  public static final TimeField AFK_START = transientField("afkStart");

  /**
   * The amount of time the player has been AFK, this will return -1 if the player is still afk when
   * this field is checked, it is only updated after the player un-afks
   */
  public static final TimeField AFK_TIME = transientField("afkTime");

  /**
   * The first time when the user began owning a market
   */
  public static final TimeField MARKET_OWNERSHIP_STARTED
      = new TimeField("market_ownershipBegan", true);

  /**
   * The last time this user bought/abandoned a market
   */
  public static final TimeField MARKET_LAST_ACTION = new TimeField("market_lastAction", true);

  public static final TimeField LAST_MOVEIN = new TimeField("lastMoveIn", true);

  private final String key;
  private final int id;

  private final boolean serialized;

  private TimeField(String key, boolean serialized) {
    this.key = key;
    this.serialized = serialized;

    Holder<TimeField> holder = REGISTRY.register(key, this);
    this.id = holder.getId();
  }

  public static void freeze() {
    REGISTRY.freeze();
  }

  public static TimeField field(String key) {
    return new TimeField(key, false);
  }

  public static TimeField transientField(String key) {
    return new TimeField(key, true);
  }
}