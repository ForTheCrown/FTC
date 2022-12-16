package net.forthecrown.user.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.registry.FtcKeyed;

/**
 * A field {@link UserTimeTracker} that tracks a timestamp
 * or an amount of time.
 */
@RequiredArgsConstructor
@Getter
public enum TimeField implements FtcKeyed {
    /**
     * Time stamp of when the user is next allowed to use
     * a command like /home or /tpa
     */
    NEXT_TELEPORT               ("nextAllowedTeleport",     false),

    /**
     * The first time the player joined this server
     */
    FIRST_JOIN                  ("firstJoined",             true ),

    /**
     * The last time the user was loaded for any reason
     */
    LAST_LOADED                 ("lastLoad",                true ),

    /**
     * The last time the user logged in to the server
     */
    LAST_LOGIN                  ("lastJoin",                true ),

    /**
     * The last time the player went AFK, not serialized
     */
    AFK_START                   ("afkStart",                false),

    /**
     * The amount of time the player has been AFK, this will return
     * -1 if the player is still afk when this field is checked,
     * it is only updated after the player un-afks
     */
    AFK_TIME                    ("afkTime",                 false),

    /**
     * The first time when the user began owning a market
     */
    MARKET_OWNERSHIP_STARTED    ("market_ownershipBegan",   true ),

    /**
     * The last time this user bought/abandoned a market
     */
    MARKET_LAST_ACTION          ("market_lastAction",       true ),

    LAST_MOVEIN                 ("lastMoveIn",              true );

    /**
     * The serialized key of this timestamp, used in JSON
     */
    private final String key;

    /**
     * Determines whether the field is serialized to
     * JSON or not
     */
    private final boolean serialized;
}