package net.forthecrown.user;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public final class TimeField {

  private static final AtomicInteger idGenerator = new AtomicInteger();
  private static boolean frozen;

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
    this.id = idGenerator.getAndIncrement();
    this.serialized = serialized;
  }

  public static void freeze() {
    frozen = true;
  }

  private static void ensureNotFrozen() {
    Preconditions.checkState(!frozen, "Time field creation is frozen");
  }

  public static TimeField field(String key) {
    ensureNotFrozen();
    return new TimeField(key, false);
  }

  public static TimeField transientField(String key) {
    ensureNotFrozen();
    return new TimeField(key, true);
  }
}